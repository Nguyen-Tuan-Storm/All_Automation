FROM ubuntu:20.04

# Install dependencies
RUN apt-get update && apt-get install -y \
    openjdk-11-jdk \
    wget \
    unzip \
    libgl1-mesa-glx \
    libpulse0 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Install Android SDK
RUN mkdir -p /sdk/cmdline-tools
WORKDIR /sdk/cmdline-tools
RUN wget https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip -O cmdline-tools.zip \
    && unzip cmdline-tools.zip -d tools \
    && rm cmdline-tools.zip

ENV ANDROID_SDK_ROOT=/sdk
ENV PATH=$PATH:/sdk/cmdline-tools/tools/bin:/sdk/platform-tools

# Accept licenses
RUN yes | /sdk/cmdline-tools/tools/bin/sdkmanager --licenses

# Install emulator and system image
RUN /sdk/cmdline-tools/tools/bin/sdkmanager "emulator" "platform-tools" "platforms;android-31" "system-images;android-31;google_apis;x86_64"

# Create AVD
RUN echo "no" | /sdk/cmdline-tools/tools/bin/avdmanager create avd -n test -k "system-images;android-31;google_apis;x86_64"

# Install additional packages
RUN apt-get -qqy update && apt-get -qqy install --no-install-recommends \
    ffmpeg \
    feh \
    libxcomposite-dev \
    menu \
    openbox \
    x11vnc \
    xterm \
 && apt autoremove -y \
 && apt clean all \
 && rm -rf /var/lib/apt/lists/*

# Install noVNC and websockify
ENV NOVNC_VERSION="1.4.0" \
    WEBSOCKIFY_VERSION="0.11.0" \
    OPT_PATH="/opt"
RUN  wget -nv -O noVNC.zip "https://github.com/novnc/noVNC/archive/refs/tags/v${NOVNC_VERSION}.zip" \
 && unzip -x noVNC.zip \
 && rm noVNC.zip  \
 && mv noVNC-${NOVNC_VERSION} ${OPT_PATH}/noVNC \
 && wget -nv -O websockify.zip "https://github.com/novnc/websockify/archive/refs/tags/v${WEBSOCKIFY_VERSION}.zip" \
 && unzip -x websockify.zip \
 && mv websockify-${WEBSOCKIFY_VERSION} ${OPT_PATH}/noVNC/utils/websockify \
 && rm websockify.zip \
 && ln ${OPT_PATH}/noVNC/vnc.html ${OPT_PATH}/noVNC/index.html

# Set environment variables for VNC
ENV DISPLAY=:0 \
    SCREEN_NUMBER=0 \
    SCREEN_WIDTH=1600 \
    SCREEN_HEIGHT=900 \
    SCREEN_DEPTH=24+32 \
    VNC_PORT=5900 \
    WEB_VNC_PORT=6080

EXPOSE 5900 6080

# Create working directory
ENV WORK_PATH="/home/androidusr"
ENV SCRIPT_PATH="docker-android"
ENV APP_PATH=${WORK_PATH}/${SCRIPT_PATH}
RUN mkdir -p ${APP_PATH}

# Copy necessary files
COPY mixins ${APP_PATH}/mixins
COPY cli ${APP_PATH}/cli
RUN chown -R 1300:1301 ${APP_PATH} \
 && pip install --quiet -e ${APP_PATH}/cli

# Configure OpenBox
RUN echo ${APP_PATH}/mixins/configs/display/.fehbg >> /etc/xdg/openbox/autostart

# Use created user
USER 1300:1301
ENV LOG_PATH=${WORK_PATH}/logs \
    WEB_LOG_PORT=9000
EXPOSE 9000
RUN mkdir -p ${LOG_PATH}
RUN mkdir -p "${WORK_PATH}/.config/Android Open Source Project" \
 && echo "[General]\nshowNestedWarning=false\n" > "${WORK_PATH}/.config/Android Open Source Project/Emulator.conf"

# Set the stop signal
STOPSIGNAL SIGTERM

# Set device type
ENV DEVICE_TYPE=emulator

# Run the application
ENTRYPOINT ["/home/androidusr/docker-android/mixins/scripts/run.sh"]
