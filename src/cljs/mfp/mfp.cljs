;    ______________________________________________________________________
;   |:..                                                      ``:::%%%%%%HH|
;   |%%%:::::..    M u s i c   f o r   p r o g r a m m i n g     `:::::%%%%|
;   |HH%%%%%:::::....._______________________________________________::::::|

(ns mfp
  (:require [clojure.string :as string]))

(println (string/join
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

(def junk ["#", "@", "%", "*", "&amp;", "&lt;", "&gt;", "_", "=", "+", "[",
           "]", "|", "-", "!", "?","X"])

(defn rand-int [min, max] (Math/round (+ min (* (Math/random) (- max min)))))

(defn replace-chars [ch]
  (if (> (rand-int 0 100) 85) (junk (rand-int 0 16)) ch))

(defn glitch-tick []
  (let [text (activities (rand-int 0 (- (count activities) 2)))
        characters (string/split text #"")
        glitched (string/join (map replace-chars characters))]
    (set! (.-innerHTML (.getElementById js/document "glitch")) glitched)))

(defn cycle-theme []
  (let [style-link (.getElementById js/document "styleLink")
        new-link (if (string/ends-with? (.-href style-link) "style.css")
                     "css/style-alt.css"
                     "css/style.css")]
      (.setAttribute style-link "href" new-link)))

(defn init-glitch []
  ; Set random timeout on the glitch-tick.
  ((fn loop-tick [] (do (glitch-tick)
                        (js/setTimeout loop-tick (rand-int 16 400))))))

(defn format-hhmmss [duration]
  (let [tostr (fn [xs] (if (< xs 10) (str "0" xs) xs))
        secs (int duration)
        hours (Math/floor (/ secs 3600))
        minutes (Math/floor (/ (- secs (* hours 3600)) 60))
        seconds (- secs (* hours 3600) (* minutes 60))]
    (str (tostr hours) \: (tostr minutes) \: (tostr seconds))))


(def context
  (let [Constructor (or (.-AudioContext js/window)
                        (.-webkitAudioContext js/window)
                        nil)]
    (Constructor.)))

(defn boop []
  (if context
    (let [oscill (.createOscillator context)
          gain (.createGain context)]
      (do
        (set! (.-type oscill) "square")
        (set! (.-value (.-frequency oscill)) 5555)
        (set! (.-value (.-gain gain)) 0.125)
        (.connect oscill gain)
        (.connect gain (.-destination context))
        (.start oscill (.-currentTime context))
        (.stop oscill (+ 0.025 (.-currentTime context)))))))

(defn get-player []
  {:timeout nil
   :auto-update false
   :state :unloaded
   :time-display (.getElementById js/document "time-display")
   :audio (.getElementById js/document "audio")
   :play-pause-btn (.getElementById js/document "play-pause-btn")
   :stop-btn (.getElementById js/document "stop-btn")
   :rewind-btn (.getElementById js/document "rewind-btn")
   :forward-btn (.getElementById js/document "forward-btn")})

(defn play-pause [player]
  (let [_player (if (= (player :state) :stopped)
                    (assoc player :state :paused)
                    player)]
  (do
    (boop)
    (cond
      (= (player :state) :stopped)
      (do
        (.add (.-classList (player :stop-btn)) "active")))
    (if (= (_player :state) :paused)
      (do
        (println "bbb")
        (assoc player :state :play)
        (.play (player :audio))))
    )))

(defn init-audio [player]
  (let [player (assoc player :state :stopped)]
  (do
    (set! (.-innerHTML (player :time-display))
          (format-hhmmss (.-duration (player :audio))))
    (.add (.-classList (player :play-pause-btn)) "active")
    (.addEventListener (player :play-pause-btn)
                       "click" #(play-pause player) false))))

(defn init-player [player]
  (do
    (set! (.-onloadedmetadata (player :audio)) #(init-audio player)))
    (.load (player :audio)))

(defn init []
  (let [player (get-player)]
    (do
      (init-player player)
      ; Add theme cycling.
      (let [theme-link (.getElementById js/document "themeLink")]
        (.addEventListener theme-link "click" cycle-theme false))
      ; |  _  |
      ; |=(o)=|    Sienar Fleet Systems'
      ; |     |    TIE/In Space Superiority Starfighter
      (boop)
      ; Initialize text glitch;
      (init-glitch))))

(.addEventListener js/window "DOMContentLoaded" init)

