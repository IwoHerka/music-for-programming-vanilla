;    ______________________________________________________________________
;   |:..                                                      ``:::%%%%%%HH|
;   |%%%:::::..    M u s i c   f o r   p r o g r a m m i n g     `:::::%%%%|
;   |HH%%%%%:::::....._______________________________________________::::::|

(ns mfp
  (:require [clojure.string :as s]
            [episodes :refer [episodes compilers]]
            [utils :refer [elem get-compiler-li get-synopsis-li
                           get-episode-li prepad-0 format-hhmmss]]))

(println (s/join
  ["Hey there!\nIf you're looking for the code then check out: "
   "github.com/iwoherka/music-for-programming.\n"
   "This little app was written in ClojureScript."]))

(def activities
  ["coding", "knitting", "drawing", "designing", "planning", "writing",
   "writing",  "concluding", "programming", "sleeping", "working", "thinking",
   "sewing", "sketching", "ruminating", "deliberating", "pondering",
   "contemplating", "abstracting", "resting", "stydying", "optimising",
   "refactoring", "simplifying", "decoupling", "debugging", "configuring",
   "streamlining", "searching", "tweaking", "editing", "practicing"])

(def junk
  ["#", "@", "%", "*", "&amp;", "&lt;", "&gt;", "_", "=", "+", "[", "]", "|",
   "-", "!", "?","X"])

(def themes
  ["css/solarized.css"
   "css/blue.css"
   "css/other.css"])

(def context
  "AudioContext of webkitAudioContext, if the first one is not available."
  (let [Constructor (or (.-AudioContext js/window)
                        (.-webkitAudioContext js/window)
                        nil)]
    (Constructor.)))

(def glitch-counter (atom 30))

(defn shake
  "With probability 15%, replaces the character with random junk."
  [ch]
  (if (<= (rand) 0.15) (rand-nth junk) ch))

(defn glitch-tick
  "Generates and sets next title."
  []
  (let [text (rand-nth activities)
        characters (s/split text #"")
        glitched (s/join (map shake characters))]
    (set! (.-innerHTML (elem "glitch")) glitched)))

(defn cycle-theme
  "Cycle to the next CSS theme."
  [player]
  ; Update theme counter.
  (swap! player assoc :theme (rem (inc (@player :theme)) (count themes)))
  (let [style-link (elem "styleLink")
        new-link (themes (@player :theme))]
    (.setAttribute style-link "href" new-link)))

(defn loop-tick
  "Starts looping glitch-tick every [16-400] ms."
  []
  (glitch-tick)
  (if (> @glitch-counter 0)
    (do
      (swap! glitch-counter dec)
      (js/setTimeout loop-tick (+ (rand-int 384) 16)))
    (set! (.-innerHTML (elem "glitch")) "programming")))

(defn init-glitch
  "Sets random timeout on the glitch-tick."
  []
  (loop-tick))

(defn boop []
  (if context
    (let [oscill (.createOscillator context)
          gain (.createGain context)]
      (set! (.-type oscill) "square")
      (set! (.-value (.-frequency oscill)) 5555)
      (set! (.-value (.-gain gain)) 0.125)
      (.connect oscill gain)
      (.connect gain (.-destination context))
      (.start oscill (.-currentTime context))
      (.stop oscill (+ 0.025 (.-currentTime context))))))

(defn -update [player]
  ; Set time display (current time).
  (set! (.-innerHTML (@player :time-display))
        (format-hhmmss (.-currentTime (@player :audio))))
  ; Check if finished playing, and stop the player if so.
  (if
    (= (.-currentTime (@player :audio)) (.-duration (@player :audio)))
    (stop player)))

(defn loop-player-update [player]
  (if (@player :auto-update)
    (do
      (-update player)
      (js/setTimeout #(loop-player-update player) 1000))))

(defn get-player []
  (atom {:episode 0
         :theme 0
         :timeout nil
         :auto-update false
         :state :unloaded
         :time-display (elem "time-display")
         :audio (elem "audio")
         :play-pause-btn (elem "play-pause-btn")
         :stop-btn (elem "stop-btn")
         :rewind-btn (elem "rewind-btn")
         :forward-btn (elem "forward-btn")
         :next-btn (elem "next")
         :previous-btn (elem "previous")
         :stop-btn-listener nil
         :rewind-btn-listener nil
         :forward-btn-listener nil
         :next-btn-listener nil
         :previous-btn-listener nil}))

(defn stop [player]
  (boop) ; Play a little sound.
  (swap! player assoc :state :stopped)
  (.pause (@player :audio))
  (set! (.-currentTime (@player :audio)) 0)
  (js/clearTimeout loop-tick)
  (swap! player assoc :auto-update false)
  (-update player)
  (set! (.-innerHTML (@player :play-pause-btn)) "-play")
  (.removeEventListener (@player :stop-btn) "click" (@player :stop-btn-listner))
  (.remove (.-classList (@player :stop-btn)) "active")
  (.removeEventListener (@player :rewind-btn) "click" (@player :rewind-btn-listner))
  (.remove (.-classList (@player :rewind-btn)) "active")
  (.removeEventListener (@player :forward-btn) "click" (@player :forward-btn-listner))
  (.remove (.-classList (@player :forward-btn)) "active")
  (println "stop"))

(defn rewind [player]
  ; Calculate time delta.
  (let [time (max 0 (- (.-currentTime (@player :audio)) 30))]
    (set! (.-currentTime (@player :audio)) time))
  (-update player))

(defn forward [player]
  ; Calculate time delta.
  (let [time (min (.-duration (@player :audio))
                  (+ (.-currentTime (@player :audio)) 30))]
    (set! (.-currentTime (@player :audio)) time))
  (-update player))

(defn play-pause [player]
  "Play/pause button handler."
  (boop)
  ; Check if player is stopped.
  (if (= (@player :state) :stopped)
    (do
      (swap! player assoc :state :paused)
      (.add (.-classList (@player :stop-btn)) "active")
      (.add (.-classList (@player :rewind-btn)) "active")
      (.add (.-classList (@player :forward-btn)) "active")
      ; Check if listeners were already created. If yes, reuse them.
      (if (= (@player :stop-btn-listener) nil)
        (do
          (swap! player assoc :stop-btn-listener #(stop player))
          (.addEventListener (@player :stop-btn)
                             "click" (@player :stop-btn-listener) false)))
      (if (= (@player :rewind-btn-listener nil))
        (do
          (swap! player assoc :rewind-btn-listener #(rewind player))
          (.addEventListener (@player :rewind-btn)
                             "click" (@player :rewind-btn-listener) false)))
      (if (= (@player :forward-btn-listener nil))
        (do
          (swap! player assoc :forward-btn-listener #(forward player))
          (.addEventListener (@player :forward-btn)
                             "click" (@player :forward-btn-listener) false)))))
  (if (= (@player :state) :paused)
    (do
      ; Pause audio.
      (swap! player assoc :state :play)
      (.play (@player :audio))
      (swap! player assoc :auto-update true)
      (loop-player-update player)
      (set! (.-innerHTML (@player :play-pause-btn)) "-pause"))
    (do
      ; Resume audio.
      (swap! player assoc :state :paused)
      (.pause (@player :audio))
      (swap! player assoc :auto-update false)
      (loop-player-update player)
      (set! (.-innerHTML (@player :play-pause-btn)) "-resume"))))

(defn init-audio
  "Initialize audio player."
  [player]
  ; Set state to stopped.
  (swap! player assoc :state :stopped)
  ; Initially display audio duration.
  (set! (.-innerHTML (@player :time-display))
        (format-hhmmss (.-duration (@player :audio))))
  (loop-player-update player)
  ; Initialize play-pause button.
  (.add (.-classList (@player :play-pause-btn)) "active")
  (.addEventListener (@player :play-pause-btn)
                     "click" #(play-pause player) false)
  (.addEventListener (@player :next-btn)
                     "click" #(next-episode player) false)
  (.addEventListener (@player :previous-btn)
                     "click" #(previous-episode player) false))

(defn init-episodes [eps]
  (let [ol (elem "episodes")
        strs (apply str (map get-episode-li (map vector eps (range))))]
    (set! (.-innerHTML ol) strs)))

(defn next-episode
  [player]
  (do
    (stop player))
    (init (+ 1 (@player :episode))))

(defn previous-episode
  [player]
  (do
    (stop player)
    ; XXX: Better solution?
    (init (- (@player :episode) 1))))

(defn init-synopsis [ep]
  (let [ol (elem "synopsis")]
    (set! (.-innerHTML ol) (apply str (map get-synopsis-li (ep :track-list))))))

(defn init-compilers [compilers]
  (let [ol (elem "compilers")
        strs (apply str (map get-compiler-li compilers))]
    (set! (.-innerHTML ol) strs)))

(defn set-track [episode]
  (set! (.-innerHTML (elem "title")) (episode :title))
  (set! (.-innerHTML (elem "duration")) (episode :duration))
  (set! (.-innerHTML (elem "compiled-by")) (episode :compiled-by))
  (set! (.-src (elem "audio")) (episode :link)))

(defn init [episode]
  (let [player (get-player)]
    ; Add theme cycling.
    (let [theme-link (elem "themeLink")]
      (.addEventListener theme-link "click" #(cycle-theme player) false))
    ; |  _  |
    ; |=(o)=|    Sienar Fleet Systems'
    ; |     |    TIE/In Space Superiority Starfighter
    (boop)
    (init-compilers compilers)
    (init-synopsis (episodes episode))
    (set-track (episodes episode))
    (init-episodes episodes)
    ; Initialize audio player when metadata is loaded.
    (set! (.-onloadedmetadata (@player :audio)) #(init-audio player))
    (.load (@player :audio))
    (loop-tick)))

(.addEventListener js/window "DOMContentLoaded" #(init 0))
