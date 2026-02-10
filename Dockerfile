FROM maven:3.9.6-eclipse-temurin-21 AS jar_build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Extract the JAR
RUN java -Djarmode=layertools -jar target/*.jar extract

FROM ubuntu:22.04 AS opencv_builder

ENV OPENCV_VERSION=4.9.0
ENV INSTALL_DIR=/usr/local

# Install only what's needed for compilation
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential cmake git openjdk-21-jdk unzip \
    libjpeg-dev libpng-dev libtiff-dev libavcodec-dev libavformat-dev libswscale-dev \
    libv4l-dev libtbb-dev libatlas-base-dev \
    && rm -rf /var/lib/apt/lists/*

# Download, extract, and copy OpenCV Java bindings
WORKDIR /opt/opencv_build

RUN git clone https://github.com/opencv/opencv.git -b ${OPENCV_VERSION} --depth 1 \
    && git clone https://github.com/opencv/opencv_contrib.git -b ${OPENCV_VERSION} --depth 1

WORKDIR /opt/opencv_build/opencv/build

RUN cmake \
    -D CMAKE_BUILD_TYPE=RELEASE \
    -D CMAKE_INSTALL_PREFIX=${INSTALL_DIR} \
    -D BUILD_JAVA=ON \
    -D BUILD_FAT_JAVA_LIB=OFF \
    -D BUILD_SHARED_LIBS=OFF \
    -D OPENCV_EXTRA_MODULES_PATH=/opt/opencv_build/opencv_contrib/modules \
    -D BUILD_EXAMPLES=OFF \
    -D BUILD_TESTS=OFF \
    -D BUILD_PERF_TESTS=OFF \
    -D BUILD_opencv_python2=OFF \
    -D BUILD_opencv_python3=OFF \
    -D WITH_GTK_2_X=OFF \
    -D WITH_FFMPEG=ON \
    -D WITH_V4L=ON ../ \
    && make -j$(nproc) && make install

# Consolidate artifacts to a known location
RUN mkdir -p /opt/opencv_artifacts/lib && \
    cp $(find /usr/local -name "opencv-*.jar") /opt/opencv_artifacts/ && \
    cp $(find /usr/local -name "libopencv_java*.so") /opt/opencv_artifacts/lib/

# AWS Lambda Runtime
FROM public.ecr.aws/lambda/java:21

WORKDIR /var/task

# Runtime optimization: Install only shared libraries (no -dev versions)
RUN microdnf update -y && microdnf install -y \
    libjpeg-turbo libpng libtiff mesa-libGL tbb \
    && microdnf clean all

# Copy compiled OpenCV artifacts
RUN mkdir -p /var/task/lib
COPY --from=opencv_builder /opt/opencv_artifacts/*.jar /var/task/
COPY --from=opencv_builder /opt/opencv_artifacts/lib/*.so /var/task/lib/

# Copy application layers - Exploded JAR for faster startup
COPY --from=jar_build /app/dependencies/ ./
COPY --from=jar_build /app/spring-boot-loader/ ./
COPY --from=jar_build /app/snapshot-dependencies/ ./
COPY --from=jar_build /app/application/ ./

# Set AWS-specific environment variables for better startup
ENV JAVA_OPTS="-Djava.library.path=/var/task/lib -XX:TieredStopAtLevel=1"

# Use JarLauncher for exploded JAR
ENTRYPOINT ["/usr/bin/java", "-Djava.library.path=/var/task/lib", "-XX:TieredStopAtLevel=1", "org.springframework.boot.loader.launch.JarLauncher"]