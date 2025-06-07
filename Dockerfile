FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM ubuntu:22.04 AS opencv_builder

ENV OPENCV_VERSION=4.9.0
ENV INSTALL_DIR=/usr/local

RUN apt-get update

# DEBUGGING STEP 2: Install core build essentials first
# If this fails, the issue is fundamental.
RUN apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    git \
    openjdk-21-jdk \
    unzip \
    # Temporarily remove other libraries to narrow down the problem
    && echo "Core dependencies installed successfully."

# DEBUGGING STEP 3: Install OpenCV libraries in smaller batches
# This helps identify which specific library is causing the problem.
# Add more RUN lines as needed to isolate.
RUN apt-get install -y --no-install-recommends \
    libjpeg-dev \
    libpng-dev \
    libtiff-dev \
    && echo "Image format libraries installed successfully."

RUN apt-get install -y --no-install-recommends \
    libavcodec-dev \
    libavformat-dev \
    libswscale-dev \
    libv4l-dev \
    libxvidcore-dev \
    libx264-dev \
    libgstreamer1.0-dev \
    libgstreamer-plugins-base1.0-dev \
    && echo "Video libraries installed successfully."

RUN apt-get install -y --no-install-recommends \
    libgtk-3-dev \
    libgl1-mesa-dev \
    libglu1-mesa-dev \
    libtbb-dev \
    libdc1394-dev \
    libatlas-base-dev \
    gfortran \
    python3-dev \
    python3-numpy \
    && echo "Other core dependencies installed successfully."

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
    -D BUILD_SHARED_LIBS=OFF \
    -D OPENCV_EXTRA_MODULES_PATH=/opt/opencv_build/opencv_contrib/modules \
    -D BUILD_EXAMPLES=OFF \
    -D BUILD_TESTS=OFF \
    -D BUILD_PERF_TESTS=OFF \
    -D BUILD_opencv_python2=OFF \
    -D BUILD_opencv_python3=OFF \
    -D WITH_GTK_2_X=OFF \
    -D WITH_FFMPEG=ON \
    -D WITH_V4L=ON \
    ../

RUN make -j$(nproc)
RUN make install

RUN export ACTUAL_OPENCV_JAR_NAME="opencv-${OPENCV_VERSION//./}.jar" \
    && echo "Actual OpenCV Java JAR name is: $ACTUAL_OPENCV_JAR_NAME" \
    && echo "ENV ACTUAL_OPENCV_JAR_NAME=$ACTUAL_OPENCV_JAR_NAME" >> /etc/profile

RUN ls -la ${INSTALL_DIR}/share/java/opencv4/
RUN ls -la ${INSTALL_DIR}/lib/
RUN ls -la ${INSTALL_DIR}/share/java/opencv4/opencv-${OPENCV_VERSION//./}.jar

RUN ldconfig

FROM ubuntu:22.04

ENV OPENCV_VERSION=4.9.0
ENV INSTALL_DIR=/usr/local

ENV ACTUAL_OPENCV_JAR_NAME="opencv-490.jar"

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

COPY --from=opencv_builder ${INSTALL_DIR}/share/java/opencv4/${ACTUAL_OPENCV_JAR_NAME} /app/
COPY --from=build /app/target/*.jar /app/facedec.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","--enable-preview","-jar","facedec.jar"]