FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM gocv/opencv:4.9.0-ubuntu-22.04



RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jre-headless \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

ENV OPENCV_VERSION=4.9.0
ENV OPENCV_URL=https://github.com/opencv/opencv/releases/download/${OPENCV_VERSION}/opencv-${OPENCV_VERSION}-linux.zip
ENV OPENCV_DIR=/opt/opencv

# Download, extract, and copy OpenCV Java bindings
RUN mkdir -p ${OPENCV_DIR} \
    && wget -O /tmp/opencv.zip ${OPENCV_URL} \
    && unzip /tmp/opencv.zip -d /tmp/ \
    && mv /tmp/opencv-${OPENCV_VERSION}/* ${OPENCV_DIR}/ \
    && rm /tmp/opencv.zip \
    && rm -rf /tmp/opencv-${OPENCV_VERSION} # Clean up downloaded files

RUN cp ${OPENCV_DIR}/build/java/opencv-${OPENCV_VERSION}.jar /usr/local/share/java/opencv4.jar \
    && mkdir -p /usr/local/share/java/opencv4/ \
    # The native .so library for Java is typically in `build/lib` or `build/java/lib`
    && cp ${OPENCV_DIR}/build/lib/libopencv_java${OPENCV_VERSION/./}.so /usr/local/share/java/opencv4/

WORKDIR /app

#ENV LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
ENV JAVA_OPTS="-Djava.library.path=/usr/local/share/java/opencv4"

ENV LD_LIBRARY_PATH=/usr/local/share/java/opencv4:/usr/local/lib:$LD_LIBRARY_PATH


COPY --from=build /app/target/*.jar /app/facedec.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","--enable-preview","-jar","facedec.jar"]