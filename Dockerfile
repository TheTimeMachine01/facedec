# Define the source of OpenCV artifacts explicitly
FROM ghcr.io/thetimemachine01/opencv-base-image:latest AS opencv_source

# Build the application JAR
FROM maven:3.9.6-eclipse-temurin-21 AS jar_build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
# Extract layers
RUN java -Djarmode=layertools -jar target/*.jar extract

# Final Application Image
# We use the raw Lambda Java 21 image and explicitly copy what we need
FROM public.ecr.aws/lambda/java:21

# Copy Lambda Web Adapter
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.4 /lambda-adapter /opt/extensions/lambda-adapter

WORKDIR /var/task

# Runtime optimization: Install only shared libraries (no -dev versions)
# These are required by OpenCV
RUN microdnf update -y && microdnf install -y \
    libjpeg-turbo libpng libtiff mesa-libGL tbb \
    && microdnf clean all

# 1. Copy OpenCV artifacts from our custom base image
# We explicitly copy them to ensure they are present and correct
COPY --from=opencv_source /var/task/opencv-490.jar /var/task/
# Create lib dir just in case
RUN mkdir -p /var/task/lib
COPY --from=opencv_source /var/task/lib/libopencv_java490.so /var/task/lib/

# 2. Copy application layers
COPY --from=jar_build /app/dependencies/ ./
COPY --from=jar_build /app/spring-boot-loader/ ./
COPY --from=jar_build /app/snapshot-dependencies/ ./
COPY --from=jar_build /app/application/ ./

# 3. Environment Configuration
ENV PORT=8080
# Ensure java.library.path points to where we put the .so file
ENV JAVA_OPTS="-Djava.library.path=/var/task/lib -XX:TieredStopAtLevel=1 -Xmx2500m"
ENV LC_ALL=C

# Use JarLauncher
ENTRYPOINT ["java", "-Djava.library.path=/var/task/lib", "--enable-preview", "-XX:TieredStopAtLevel=1", "-Xmx2500m", "org.springframework.boot.loader.launch.JarLauncher"]