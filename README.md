# Gizmo Grokker
What is this project for???

## Building
### Dependencies
* Java 8
```shell
brew tap AdoptOpenJDK/openjdk
brew cask install adoptopenjdk8
```
* Gradle
``` shell
brew install gradle
```
* Android SDK
``` shell
brew cask install android-sdk android-platform-tools
echo 'export ANDROID_SDK_ROOT="/usr/local/share/android-sdk"' >> ~/.zshrc
echo 'export ANDROID_HOME="/usr/local/share/android-sdk"' >> ~/.zshrc
source ~/.zshrc

/usr/local/share/android-sdk/tools/bin/sdkmanager --install "build-tools;28.0.3"
/usr/local/share/android-sdk/tools/bin/sdkmanager --install "platforms;android-28"
```
* Kotlin
``` shell
brew install kotlin
```