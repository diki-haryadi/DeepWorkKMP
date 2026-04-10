# DeepWork Makefile (Compose Multiplatform: Android + Desktop JVM)

# Android SDK: pakai ANDROID_HOME kalau ada, else default macOS
ANDROID_SDK ?= $(shell echo $${ANDROID_HOME:-$$HOME/Library/Android/sdk})
EMULATOR := $(ANDROID_SDK)/emulator/emulator

APP_ID ?= co.id.relay.digitals
MAIN_ACTIVITY ?= $(APP_ID)/co.id.relay.digitals.MainActivity
DRAWABLE := composeApp/src/commonMain/composeResources/drawable
APK_DEBUG := composeApp/build/outputs/apk/debug/composeApp-debug.apk

.PHONY: help build build-release run run-gradle install-debug install-help devices logcat logcat-app avds emulator desktop-run desktop-package check gradle-clean svg2xml svg2xml-all svg2xml-dir

help:
	@echo "DeepWork commands:"
	@echo "  make build                  Build Android debug APK"
	@echo "  make build-release          Build Android release APK"
	@echo "  make run [DEVICE=id]        Build + adb install -r -t + launch (disarankan)"
	@echo "  make run-gradle [DEVICE=id] Pakai ./gradlew installDebug (alternatif)"
	@echo "  make install-debug [DEVICE=id] Hanya install APK debug (butuh make build dulu)"
	@echo "  make install-help           Jika INSTALL_FAILED_USER_RESTRICTED / dibatalkan HP"
	@echo "  make devices                List Android devices"
	@echo "  make logcat [DEVICE=id]     Show full logcat"
	@echo "  make logcat-app [DEVICE=id] Show logcat filtered by app PID"
	@echo "  make avds                   List available Android AVD"
	@echo "  make emulator [AVD=name]    Start emulator (default: first AVD)"
	@echo "  make desktop-run            Run Desktop JVM app"
	@echo "  make desktop-package        Package Desktop app"
	@echo "  make check                  Compile Android + JVM"
	@echo "  make gradle-clean           Clean composeApp"

# adb dengan serial eksplisit (hindari salah device bila banyak terhubung)
ifeq ($(strip $(DEVICE)),)
  ADB := adb
else
  ADB := adb -s $(DEVICE)
endif

build:
	@echo "Building debug APK..."
	@./gradlew :composeApp:assembleDebug
	@echo "Done."

build-release:
	@echo "Building release APK..."
	@./gradlew :composeApp:assembleRelease
	@echo "Done."

install-help:
	@echo "INSTALL_FAILED_USER_RESTRICTED / Install canceled by user — biasanya aturan di HP, bukan Gradle."
	@echo ""
	@echo "Xiaomi / MIUI / Redmi (sering):"
	@echo "  Pengaturan -> Setelan tambahan -> Opsi pengembang:"
	@echo "    - Nyalakan \"Instal melalui USB\" / Install via USB"
	@echo "    - Nyalakan \"Debugging USB (setelan keamanan)\" jika ada"
	@echo "  Cabut USB, sambung lagi; saat install, cek layar HP — tap Izinkan."
	@echo ""
	@echo "Umum:"
	@echo "  - Matikan Pembatasan / Parental controls yang memblokir sideload"
	@echo "  - Mode kerja / profil kerja: coba perangkat tanpa profil kerja"
	@echo "  - Uji: adb -s SERIAL install -r -t $(APK_DEBUG)"

install-debug:
	@test -f "$(APK_DEBUG)" || (echo ">>> Jalankan dulu: make build   (APK tidak ada: $(APK_DEBUG))"; exit 1)
	@$(ADB) devices
	@echo "Installing $(APK_DEBUG) ..."
	@$(ADB) install -r -t "$(APK_DEBUG)"

run: build
	@$(ADB) devices
	@echo "Installing $(APK_DEBUG) ..."
	@$(ADB) install -r -t "$(APK_DEBUG)" || \
		( echo ""; \
		  echo ">>> Gagal install. Lihat: make install-help"; \
		  exit 1 )
	@$(ADB) shell am start -n "$(MAIN_ACTIVITY)"

# Alternatif: instalasi lewat Gradle (kadang serial tidak sama persis dengan adb -s)
run-gradle:
	@adb devices
	@ANDROID_SERIAL="$(DEVICE)" ./gradlew :composeApp:installDebug
	@$(ADB) shell am start -n "$(MAIN_ACTIVITY)"

devices:
	@adb devices -l

logcat:
	@echo "Logcat (device: $(or $(DEVICE),default)). Ctrl+C untuk berhenti."
	@$(ADB) logcat -v time

logcat-app:
	@pid=$$($(ADB) shell pidof -s $(APP_ID) 2>/dev/null); \
	if [ -z "$$pid" ]; then \
		echo ">>> App $(APP_ID) tidak berjalan. Jalankan dulu: make run"; \
		exit 1; \
	fi; \
	echo "Logcat PID $$pid ($(APP_ID)). Ctrl+C untuk berhenti."; \
	$(ADB) logcat -v time --pid=$$pid

avds:
	@$(EMULATOR) -list-avds

emulator:
	@if [ -z "$(AVD)" ]; then \
		AVD=$$($(EMULATOR) -list-avds | head -1); \
		if [ -z "$$AVD" ]; then echo "Tidak ada AVD. Buat dulu di Android Studio -> Device Manager."; exit 1; fi; \
		echo "Starting emulator: $$AVD"; \
		$(EMULATOR) -avd "$$AVD"; \
	else \
		echo "Starting emulator: $(AVD)"; \
		$(EMULATOR) -avd "$(AVD)"; \
	fi

desktop-run:
	@./gradlew :composeApp:run

desktop-package:
	@./gradlew :composeApp:packageDistributionForCurrentOS

check:
	@./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinJvm

gradle-clean:
	@./gradlew :composeApp:clean

# --- SVG -> Android Vector Drawable (.xml) ---
svg2xml:
	@if [ -z "$(SVG)" ]; then \
		echo "Usage: make svg2xml SVG=nama.svg   (file di $(DRAWABLE))"; \
		exit 1; \
	fi
	@test -f "$(DRAWABLE)/$(SVG)" || (echo "Tidak ada: $(DRAWABLE)/$(SVG)"; exit 1)
	@base=$$(basename "$(SVG)" .svg); \
	echo "svg2xml: $(SVG) -> $$base.xml"; \
	npx --yes svg2vectordrawable -i "$(DRAWABLE)/$(SVG)" -o "$(DRAWABLE)/$$base.xml"

svg2xml-all:
	@test -d "$(DRAWABLE)" || (echo "Tidak ada folder $(DRAWABLE)"; exit 1)
	@count=$$(ls -1 "$(DRAWABLE)"/*.svg 2>/dev/null | wc -l | tr -d ' '); \
	if [ "$$count" -eq 0 ]; then echo "Tidak ada .svg di $(DRAWABLE)"; exit 1; fi; \
	echo "svg2xml-all: $$count svg -> xml di $(DRAWABLE)..."
	npx --yes svg2vectordrawable -f "$(DRAWABLE)" -o "$(DRAWABLE)"

svg2xml-dir:
	@if [ -z "$(IN)" ] || [ -z "$(OUT)" ]; then \
		echo "Usage: make svg2xml-dir IN=folder/svg OUT=folder/xml"; \
		exit 1; \
	fi
	@mkdir -p "$(OUT)"
	npx --yes svg2vectordrawable -f "$(IN)" -o "$(OUT)"