# Define the source of OpenCV artifacts explicitly
FROM ghcr.io/thetimemachine01/opencv-base-image:latest AS opencv_source

# -----------------------------------------------------------------------------
# Stage: Library Harvester
# The OpenCV build was done on Ubuntu 22.04, which links against libjpeg.so.8.
# Amazon Linux (Runtime) provides libjpeg.so.62. We need to harvest the
# correct shared library from Ubuntu to satisfy the dynamic linker.
# -----------------------------------------------------------------------------
FROM ubuntu:22.04 AS lib_harvester
RUN apt-get update && apt-get install -y libjpeg-turbo8
# Locate libjpeg.so.8 (usually in /usr/lib/x86_64-linux-gnu/)

# -----------------------------------------------------------------------------
# Stage: Jar Build
# -----------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS jar_build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
# Extract layers
RUN java -Djarmode=layertools -jar target/*.jar extract

# -----------------------------------------------------------------------------
# Stage: Final Runtime
# -----------------------------------------------------------------------------
FROM public.ecr.aws/lambda/java:21

# Copy Lambda Web Adapter
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.4 /lambda-adapter /opt/extensions/lambda-adapter

WORKDIR /var/task

# Install system dependencies (keep these to ensure other libs like libpng/tiff are present if compatible)
RUN microdnf update -y && microdnf install -y \
    libjpeg-turbo libpng libtiff mesa-libGL tbb \
    && microdnf clean all

# 1. Copy OpenCV artifacts from our custom base image
COPY --from=opencv_source /var/task/opencv-490.jar /var/task/
# Create lib dir
RUN mkdir -p /var/task/lib
COPY --from=opencv_source /var/task/lib/libopencv_java490.so /var/task/lib/

# 2. Copy harvested libraries (libjpeg.so.8)
# We copy explicitly from the ubuntu harvester
COPY --from=lib_harvester /usr/lib/x86_64-linux-gnu/libjpeg.so.8* /var/task/lib/

# 3. Copy application layers
COPY --from=jar_build /app/dependencies/ ./
COPY --from=jar_build /app/spring-boot-loader/ ./
COPY --from=jar_build /app/snapshot-dependencies/ ./
COPY --from=jar_build /app/application/ ./

# 4. Environment Configuration
ENV PORT=8080
# Ensure java.library.path points to where we put the .so file
# We also add /var/task/lib to LD_LIBRARY_PATH so the system loader finds libjpeg.so.8 there
ENV JAVA_OPTS="-Djava.library.path=/var/task/lib -XX:TieredStopAtLevel=1 -Xmx2500m"
ENV LD_LIBRARY_PATH=/var/task/lib:/lib:/usr/lib
ENV LC_ALL=C

# Use JarLauncher
ENTRYPOINT ["java", "-Djava.library.path=/var/task/lib", "--enable-preview", "-XX:TieredStopAtLevel=1", "-Xmx2500m", "org.springframework.boot.loader.launch.JarLauncher"]