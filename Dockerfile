FROM maven:3.9.6-eclipse-temurin-21 AS jar_build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM ubuntu:22.04 AS opencv_builder

ENV OPENCV_VERSION=4.9.0
ENV INSTALL_DIR=/usr/local

ENV ACTUAL_OPENCV_JAR_NAME="opencv-490.jar"

# Install only what's needed for compilation
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential cmake git openjdk-21-jdk unzip \
    libjpeg-dev libpng-dev libtiff-dev libavcodec-dev libavformat-dev libswscale-dev \
    libv4l-dev libtbb-dev libatlas-base-dev \
    && rm -rf /var/lib/apt/lists/*


# Download, extract, and copy OpenCV Java bindings
WORKDIR /opt/opencv_build


RUN git clone https://github.com/opencv/opencv.git -b ${OPENCV_VERSION} --depth 1

WORKDIR /opt/opencv_build/opencv/build

RUN cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=${INSTALL_DIR} \
    -D BUILD_JAVA=ON -D BUILD_FAT_JAVA_LIB=OFF -D BUILD_SHARED_LIBS=OFF \
    -D BUILD_EXAMPLES=OFF -D BUILD_TESTS=OFF -D BUILD_PERF_TESTS=OFF ../ \
    && make -j$(nproc) && make install

# Optimized Runtime Stage for Docker Hub
FROM ubuntu:22.04

WORKDIR /app

# Install only the necessary shared libraries for OpenCV
RUN apt-get update && apt-get install -y --no-install-recommends \
    libjpeg8 libpng16-16 libtiff5 libavcodec58 libavformat58 libswscale5 \
    libtbb2 libatlas3-base libv4l-0 \
    && rm -rf /var/lib/apt/lists/*

# Copy artifacts from previous stages
COPY --from=jar_build /app/target/*.jar app.jar
RUN mkdir -p /app/lib
COPY --from=opencv_builder /usr/local/share/java/opencv4/*.so /app/lib/

# Environment for faster startup
ENV JAVA_OPTS="-Djava.library.path=/app/lib -XX:TieredStopAtLevel=1"

# Standard Java entrypoint
ENTRYPOINT ["java", "-XX:TieredStopAtLevel=1", "-jar", "app.jar"]