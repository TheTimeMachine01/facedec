FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM ubuntu:22.04

ENV OPENCV_VERSION=4.9.0
ENV INSTALL_DIR=/usr/local

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    git \
    openjdk-21-jdk \
    libgtk-3-dev \
    libjpeg-dev \
    libpng-dev \
    libtiff-dev \
    libavcodec-dev \
    libavformat-dev \
    libswscale-dev \
    libv4l-dev \
    libxvidcore-dev \
    libx264-dev \
    libgstreamer1.0-dev \
    libgstreamer-plugins-base1.0-dev \
    libgl1-mesa-dev \
    libglu1-mesa-dev \
    libtbb-dev \
    libdc1394-22-dev \
    libatlas-base-dev \
    gfortran \
    python3-dev \
    python3-numpy \
    unzip \
    && rm -rf /var/lib/apt/lists/* \

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Download, extract, and copy OpenCV Java bindings
WORKDIR /opt/opencv_build
RUN git clone https://github.com/opencv/opencv.git -b ${OPENCV_VERSION} --depth 1 \
    && git clone https://github.com/opencv/opencv_contrib.git -b ${OPENCV_VERSION} --depth 1

WORKDIR /opt/opencv_build/opencv/build

RUN cmake \
    -D CMAKE_BUILD_TYPE=RELEASE \
    -D CMAKE_INSTALL_PREFIX=${INSTALL_DIR} \
    -D BUILD_JAVA=ON \
    -D BUILD_FAT_JAVA_LIB=ON \
    -D OPENCV_EXTRA_MODULES_PATH=/opt/opencv_build/opencv_contrib/modules \
    -D BUILD_EXAMPLES=OFF \
    -D BUILD_TESTS=OFF \
    -D BUILD_PERF_TESTS=OFF \
    -D BUILD_opencv_python2=OFF \
    -D BUILD_opencv_python3=OFF \
    -D WITH_GTK_2_X=OFF \ # Ensure GTK3 is preferred if installed
    -D WITH_FFMPEG=ON \ # Enable FFMPEG for video support
    -D WITH_V4L=ON \ # Enable Video4Linux for camera support
    ../

RUN make -j$(nproc)
RUN make install

RUN ldconfig

FROM ubuntu:22.04

RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jre-headless \
    libgl1-mesa-glx \
    libjpeg-turbo8 \
    # Include other runtime dependencies if your specific OpenCV usage requires them
    # Based on the build stage, you might need libgtk-3-0, libavcodec58, etc.
    # Check the "ldd" command on the .so files in a running container if you face missing libraries
    && rm -rf /var/lib/apt/lists/* \

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

WORKDIR /app

#ENV LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
ENV JAVA_OPTS="-Djava.library.path=${INSTALL_DIR}/share/java/opencv4"

ENV LD_LIBRARY_PATH=${INSTALL_DIR}/lib:${INSTALL_DIR}/share/java/opencv4:$LD_LIBRARY_PATH

COPY --from=stage2 ${INSTALL_DIR}/share/java/opencv4/opencv-${OPENCV_VERSION}.jar /app/
COPY --from=build /app/target/*.jar /app/facedec.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","--enable-preview","-jar","facedec.jar"]