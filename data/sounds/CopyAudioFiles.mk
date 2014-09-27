LOCAL_PATH := frameworks/base/data/sounds

# Simple files that do not require renaming
ALARM_FILES := Alarm_Beep_01 Argon Oxygen Alarm_Beep_02 Barium Helium Platinum \
               Alarm_Beep_03 Carbon Krypton Plutonium Alarm_Buzzer Cesium Neon \
               Copernicium Neptunium Scandium Alarm_Rooster_01 Curium Nobelium \
               Alarm_Rooster_02 Fermium Osmium

NOTIFICATION_FILES := Cheeper Iridium Sirius Adara Cobalt Krypton Sirrah Aldebaran Cricket \
                      Merope SpaceSeed Altair Doink Mira Spica Alya Drip Moonbeam Star_Struck \
                      Antares Elara	Palladium Strontium Antimony Electra Pixiedust Syrma Arcturus \
                      Europa Pizzicato TaDa Argon F1_MissedCall Plastic_Pipe Talitha Ariel \
                      F1_New_MMS Polaris Tethys Beat_Box_Android F1_New_SMS Pollux Thallium \
                      Bees_Knees F1_NewVoicemail Procyon Tinkerbell Bellatrix Fluorine Proxima \
                      Titan Beryllium Fomalhaut Radon Tweeters Betelgeuse Gallium Regulus Upsilon \
                      Canopus Heaven Rhea Vega Capella Helium Rubidium Xenon Carme Hojus Salacia \
                      Zirconium Castor Iapetus Selenium Ceres IM_Me	Shaula CetiAlpha Io ShortCircuit

RINGTONE_FILES := Ganymede Ring_Classic_02 Acheron Gimme_Mo_Town Ring_Classic_03 Andromeda \
                  Girtab Ring_Classic_04 Aquila  Glacial_Groove Ring_Classic_05 ArgoNavis \
                  Gotcha Ring_Digital_01 Atria Growl Ring_Digital_02 Backroad HalfwayHome \
                  Ring_Digital_03 BeatPlucker Highwire Ring_Digital_04 BentleyDubs Hydra \
                  Ring_Digital_05 Big_Easy InsertCoin Ring_Synth_01 BirdLoop Iridium Ring_Synth_02 \
                  Bollywood Jump_Up Ring_Synth_03 Bootes Kuma Ring_Synth_04 BussaMove KzurbSonar \
                  Ring_Synth_05 CaffeineSnake Lectro_Beat Road_Trip Cairo LoopyLounge RobotsforEveryone \
                  Callisto LoveFlute RomancingTheTone Calypso_Steel Luna Safari CanisMajor Lyra Savannah \
                  CaribbeanIce Machina Scarabaeus Carina Miami_Twice Sceptrum Cassiopeia MidEvilJaunt \
                  Sedna Centaurus MildlyAlarming Seville Champagne_Edition Nairobi Shes_All_That \
                  Club_Cubano Nasqueron SilkyWay CousinJones Nassau SitarVsSitar CrayonRock NewPlayer \
                  Solarium CrazyDream Noises1 SpagnolaOrchestration CurveBall Noises2 SpringyJalopy \
                  Cygnus Noises3 Steppin_Out DancinFool NoiseyDing Terminated DearDeer No_Limits \
                  Testudo Ding Oberon Themos Dione OnTheHunt Third_Eye DonMessWivIt OrganDub \
                  Thunderfoot DontPanic Orion Triton Draco Paradise_Island TwirlAway DreamTheme \
                  Pegasus Umbriel Eastern_Sky Perseus UrsaMinor Enter_the_Nexus Phobos VeryAlarmed \
                  Eridani Playa Vespa EtherShake Pyxis Voila FreeFlight Rasalas World FriendlyGhost \
                  Revelation Zeta Funk_Yall Rigel GameOverGuitar Ring_Classic_01

EFFECT_FILES := KeypressDelete Lock VideoRecord camera_click KeypressInvalid LowBattery VideoStop \
                camera_focus KeypressReturn Media_Volume VolumeIncremental Dock KeypressSpacebar \
                Undock WirelessChargingStarted Effect_Tick KeypressStandard Unlock

PRODUCT_COPY_FILES += $(foreach fn,$(ALARM_FILES),\
	$(LOCAL_PATH)/alarms/ogg/$(fn).ogg:system/media/audio/alarms/$(fn).ogg)

PRODUCT_COPY_FILES += $(foreach fn,$(NOTIFICATION_FILES),\
	$(LOCAL_PATH)/notifications/ogg/$(fn).ogg:system/media/audio/notifications/$(fn).ogg)

PRODUCT_COPY_FILES += $(foreach fn,$(RINGTONE_FILES),\
	$(LOCAL_PATH)/ringtones/ogg/$(fn).ogg:system/media/audio/ringtones/$(fn).ogg)

PRODUCT_COPY_FILES += $(foreach fn,$(EFFECT_FILES),\
	$(LOCAL_PATH)/ui/ogg/$(fn).ogg:system/media/audio/ui/$(fn).ogg)
