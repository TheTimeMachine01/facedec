# Base image definition moved to FROM instruction

# Build the application JAR
FROM maven:3.9.6-eclipse-temurin-21 AS jar_build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Extract layers
RUN java -Djarmode=layertools -jar target/*.jar extract

# Final Application Image
# We start from our custom base image which ALREADY contains OpenCV
FROM ghcr.io/thetimemachine01/opencv-base-image:latest

# This copies the Lambda Web Adapter binary into your image
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.4 /lambda-adapter /opt/extensions/lambda-adapter

WORKDIR /var/task

# OpenCV artifacts are ALREADY in the base image at:
# /var/task/*.jar and /var/task/lib/*.so
# We do not need to copy them again.

# Copy application layers - Exploded JAR for faster startup
COPY --from=jar_build /app/dependencies/ ./
COPY --from=jar_build /app/spring-boot-loader/ ./
COPY --from=jar_build /app/snapshot-dependencies/ ./
COPY --from=jar_build /app/application/ ./

# Set AWS-specific environment variables for better startup
# Note: java.library.path is already correct in the base image, but we set it again to be sure
# Also TieredStopAtLevel=1 is good for Lambda startup speed
ENV PORT=8080
ENV JAVA_OPTS="-Djava.library.path=/var/task/lib -XX:TieredStopAtLevel=1"
ENV LC_ALL=C

# Use JarLauncher for exploded JAR
ENTRYPOINT ["java", "-Djava.library.path=/var/task/lib", "--enable-preview", "-XX:TieredStopAtLevel=1", "org.springframework.boot.loader.launch.JarLauncher"]