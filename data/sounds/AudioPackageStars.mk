#
# Stars Audio Package
#
# Include this file in a product makefile to include these audio files
#
#

LOCAL_PATH:= frameworks/base/data/sounds

# Simple files that do not require renaming
NOTIFICATION_FILES := Ariel Ceres Carme Elara Europa Iapetus Io Rhea Salacia Titan Tethys
RINGTONE_FILES := Callisto Dione Ganymede Luna Oberon Phobos Sedna Titania Triton Umbriel
EFFECT_FILES := Effect_Tick KeypressReturn KeypressInvalid KeypressDelete KeypressSpacebar KeypressStandard \
        VideoRecord VideoStop camera_click camera_focus LowBattery Dock Undock Lock Unlock WirelessChargingStarted \
        Media_Volume VolumeIncremental

PRODUCT_COPY_FILES += $(foreach fn,$(NOTIFICATION_FILES),\
        $(LOCAL_PATH)/notifications/ogg/$(fn).ogg:system/media/audio/notifications/$(fn).ogg)

PRODUCT_COPY_FILES += $(foreach fn,$(RINGTONE_FILES),\
        $(LOCAL_PATH)/ringtones/ogg/$(fn).ogg:system/media/audio/ringtones/$(fn).ogg)

PRODUCT_COPY_FILES += $(foreach fn,$(EFFECT_FILES),\
        $(LOCAL_PATH)/effects/ogg/$(fn).ogg:system/media/audio/ui/$(fn).ogg)

# Notifications
PRODUCT_COPY_FILES += \
	$(LOCAL_PATH)/notifications/ogg/Adara.ogg:system/media/audio/notifications/Adara.ogg \
	$(LOCAL_PATH)/notifications/ogg/Aldebaran.ogg:system/media/audio/notifications/Aldebaran.ogg \
	$(LOCAL_PATH)/notifications/ogg/Altair.ogg:system/media/audio/notifications/Altair.ogg \
	$(LOCAL_PATH)/notifications/ogg/Antares.ogg:system/media/audio/notifications/Antares.ogg \
	$(LOCAL_PATH)/notifications/ogg/Arcturus.ogg:system/media/audio/notifications/Arcturus.ogg \
	$(LOCAL_PATH)/notifications/ogg/Betelgeuse.ogg:system/media/audio/notifications/Betelgeuse.ogg \
	$(LOCAL_PATH)/notifications/ogg/Canopus.ogg:system/media/audio/notifications/Canopus.ogg \
	$(LOCAL_PATH)/notifications/ogg/Capella.ogg:system/media/audio/notifications/Capella.ogg \
	$(LOCAL_PATH)/notifications/ogg/Castor.ogg:system/media/audio/notifications/Castor.ogg \
	$(LOCAL_PATH)/notifications/ogg/CetiAlpha.ogg:system/media/audio/notifications/CetiAlpha.ogg \
	$(LOCAL_PATH)/notifications/ogg/Electra.ogg:system/media/audio/notifications/Electra.ogg \
	$(LOCAL_PATH)/notifications/ogg/Fomalhaut.ogg:system/media/audio/notifications/Fomalhaut.ogg \
	$(LOCAL_PATH)/notifications/ogg/Hojus.ogg:system/media/audio/notifications/Hojus.ogg \
	$(LOCAL_PATH)/notifications/ogg/Merope.ogg:system/media/audio/notifications/Merope.ogg \
	$(LOCAL_PATH)/notifications/ogg/Mira.ogg:system/media/audio/notifications/Mira.ogg \
	$(LOCAL_PATH)/notifications/ogg/Polaris.ogg:system/media/audio/notifications/Polaris.ogg \
	$(LOCAL_PATH)/notifications/ogg/Pollux.ogg:system/media/audio/notifications/Pollux.ogg \
	$(LOCAL_PATH)/notifications/ogg/Procyon.ogg:system/media/audio/notifications/Procyon.ogg \
	$(LOCAL_PATH)/notifications/ogg/Proxima.ogg:system/media/audio/notifications/Proxima.ogg \
	$(LOCAL_PATH)/notifications/ogg/Regulus.ogg:system/media/audio/notifications/Regulus.ogg \
	$(LOCAL_PATH)/notifications/ogg/Shaula.ogg:system/media/audio/notifications/Shaula.ogg \
	$(LOCAL_PATH)/notifications/ogg/Sirius.ogg:system/media/audio/notifications/Sirius.ogg \
	$(LOCAL_PATH)/notifications/ogg/Sirrah.ogg:system/media/audio/notifications/Sirrah.ogg \
	$(LOCAL_PATH)/notifications/ogg/Spica.ogg:system/media/audio/notifications/Spica.ogg \
	$(LOCAL_PATH)/notifications/ogg/Upsilon.ogg:system/media/audio/notifications/Upsilon.ogg \
	$(LOCAL_PATH)/notifications/ogg/Vega.ogg:system/media/audio/notifications/Vega.ogg

# Ringtones
PRODUCT_COPY_FILES += \
	$(LOCAL_PATH)/ringtones/ogg/Acheron.ogg:system/media/audio/ringtones/Acheron.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Andromeda.ogg:system/media/audio/ringtones/Andromeda.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Aquila.ogg:system/media/audio/ringtones/Aquila.ogg \
	$(LOCAL_PATH)/ringtones/ogg/ArgoNavis.ogg:system/media/audio/ringtones/ArgoNavis.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Bootes.ogg:system/media/audio/ringtones/Bootes.ogg \
	$(LOCAL_PATH)/ringtones/ogg/CanisMajor.ogg:system/media/audio/ringtones/CanisMajor.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Carina.ogg:system/media/audio/ringtones/Carina.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Cassiopeia.ogg:system/media/audio/ringtones/Cassiopeia.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Centaurus.ogg:system/media/audio/ringtones/Centaurus.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Cygnus.ogg:system/media/audio/ringtones/Cygnus.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Draco.ogg:system/media/audio/ringtones/Draco.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Eridani.ogg:system/media/audio/ringtones/Eridani.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Hydra.ogg:system/media/audio/ringtones/Hydra.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Iridium.ogg:system/media/audio/ringtones/Iridium.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Lyra.ogg:system/media/audio/ringtones/Lyra.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Machina.ogg:system/media/audio/ringtones/Machina.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Nasqueron.ogg:system/media/audio/ringtones/Nasqueron.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Orion.ogg:system/media/audio/ringtones/Orion.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Pegasus.ogg:system/media/audio/ringtones/Pegasus.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Perseus.ogg:system/media/audio/ringtones/Perseus.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Pyxis.ogg:system/media/audio/ringtones/Pyxis.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Rigel.ogg:system/media/audio/ringtones/Rigel.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Scarabaeus.ogg:system/media/audio/ringtones/Scarabaeus.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Sceptrum.ogg:system/media/audio/ringtones/Sceptrum.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Solarium.ogg:system/media/audio/ringtones/Solarium.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Testudo.ogg:system/media/audio/ringtones/Testudo.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Themos.ogg:system/media/audio/ringtones/Themos.ogg \
	$(LOCAL_PATH)/ringtones/ogg/UrsaMinor.ogg:system/media/audio/ringtones/UrsaMinor.ogg \
	$(LOCAL_PATH)/ringtones/ogg/Zeta.ogg:system/media/audio/ringtones/Zeta.ogg
