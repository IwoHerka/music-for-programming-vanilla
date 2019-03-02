;    ______________________________________________________________________
;   |:..                                                      ``:::%%%%%%HH|
;   |%%%:::::..    M u s i c   f o r   p r o g r a m m i n g     `:::::%%%%|
;   |HH%%%%%:::::....._______________________________________________::::::|

(ns mfp
  (:require [clojure.string :as string]
            [episodes :as data]))

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

; Generate random integer.
(defn rand-int [min, max] (Math/round (+ min (* (Math/random) (- max min)))))

; With probability N, replace character with random junk.
(defn replace-char [ch]
  (if (> (rand-int 0 100) 85) (junk (rand-int 0 16)) ch))

; Cycle to next title, generate random glitch.
(defn glitch-tick []
  (let [text (activities (rand-int 0 (- (count activities) 2)))
        characters (string/split text #"")
        glitched (string/join (map replace-char characters))]
    (set! (.-innerHTML (.getElementById js/document "glitch")) glitched)))

(defn cycle-theme []
  ; Cycle to the next CSS theme.
  (let [style-link (.getElementById js/document "styleLink")
        new-link (if (string/ends-with? (.-href style-link) "style.css")
                     "css/style-alt.css"
                     "css/style.css")]
      (.setAttribute style-link "href" new-link)))

(defn loop-tick []
  ; Start looping glitch-tick every [16-400] ms.
  (do
    (glitch-tick)
    (if (> @glitch-counter 0)
      (do
        (swap! glitch-counter dec)
        (js/setTimeout loop-tick (rand-int 16 400)))
      (set! (.-innerHTML (.getElementById js/document "glitch")) "programming"))))

(defn init-glitch []
  ; Set random timeout on the glitch-tick.
  (loop-tick))

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

(defn update [player]
  (do
    (set! (.-innerHTML (@player :time-display))
          (format-hhmmss (.-currentTime (@player :audio))))
    (if (= (.-currentTime (@player :audio)) (.-duration (@player :audio)))
      (stop player))))

(defn loop-player-update [player]
  (if (@player :auto-update)
    (do
      (update player)
      (js/setTimeout #(loop-player-update player) 1000))))

(def glitch-counter (atom 30))

(defn get-player []
  (atom {:timeout nil
         :auto-update false
         :state :unloaded
         :time-display (.getElementById js/document "time-display")
         :audio (.getElementById js/document "audio")
         :play-pause-btn (.getElementById js/document "play-pause-btn")
         :stop-btn (.getElementById js/document "stop-btn")
         :rewind-btn (.getElementById js/document "rewind-btn")
         :forward-btn (.getElementById js/document "forward-btn")
         :stop-btn-listener nil
         :rewind-btn-listener nil
         :forward-btn-listener nil}))

(defn stop [player]
  (do
    (boop)
    (swap! player assoc :state :stopped)
    (.pause (@player :audio))
    (set! (.-currentTime (@player :audio)) 0)
    (js/clearTimeout loop-tick)
    (swap! player assoc :auto-update false)
    (update player)
    (set! (.-innerHTML (@player :play-pause-btn)) "-play")
    (.removeEventListener (@player :stop-btn) "click" (@player :stop-btn-listner))
    (.remove (.-classList (@player :stop-btn)) "active")
    (.removeEventListener (@player :rewind-btn) "click" (@player :rewind-btn-listner))
    (.remove (.-classList (@player :rewind-btn)) "active")
    (.removeEventListener (@player :forward-btn) "click" (@player :forward-btn-listner))
    (.remove (.-classList (@player :forward-btn)) "active")
    (println "stop")))

(defn rewind [player]
  (do
    (let [time (max 0 (- (.-currentTime (@player :audio)) 30))]
      (set! (.-currentTime (@player :audio)) time))
    (update player)))

(defn forward [player]
  (do
    (let [time (min (.-duration (@player :audio)) (+ (.-currentTime (@player :audio)) 30))]
      (set! (.-currentTime (@player :audio)) time))
    (update player)))

(defn play-pause [player]
  ; Play/pause button handler.
  (do
    (boop)
    ; Check if player is stopped or
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
        (set! (.-innerHTML (@player :play-pause-btn)) "-resume")))))

(defn init-audio [player]
  ; Initialize audio player.
  (do
    ; Set state to stopped.
    (swap! player assoc :state :stopped)
    ; Initially display audio duration.
    (set! (.-innerHTML (@player :time-display))
          (format-hhmmss (.-duration (@player :audio))))
    (loop-player-update player)
    ; Initialize play-pause button.
    (.add (.-classList (@player :play-pause-btn)) "active")
    (.addEventListener (@player :play-pause-btn)
                       "click" #(play-pause player) false)))

(defn get-compiler-li [compinfo]
  (str "<li><a href=\"" (compinfo 1) "\">" (compinfo 0) "</a></li>"))

(defn get-synopsis-li [track]
  (str "<li id=\"#\">" (track 0) "</li>"))

(defn get-episode-li [[ep i]]
  (str "<li><b><a href=\"#\">-"
       (if (>= i 10) i (str "0" i))
       ", "
       (ep :title)
       "</a></b> [<i>"
       (string/join " | " (ep :genres))
       "</i>]</li>"))

(defn init-episodes [eps]
  (let [ol (.getElementById js/document "episodes")]
    ; (for [ep eps i (range)] (set! (.-innerHTML ol) (get-episode-li i ep)))))
    (set! (.-innerHTML ol) (apply str (map get-episode-li (map vector eps (range)))))))

(defn init-synopsis [ep]
  (let [ol (.getElementById js/document "synopsis")]
    (set! (.-innerHTML ol) (apply str (map get-synopsis-li (ep :track-list))))))

(defn init-compilers [compilers]
  (let [ol (.getElementById js/document "compilers")]
    (set! (.-innerHTML ol) (apply str (map get-compiler-li compilers)))))

(defn init-track [episode]
  (do
    (set! (.-innerHTML (.getElementById js/document "title")) (episode :title))
    (set! (.-innerHTML (.getElementById js/document "duration")) (episode :duration))
    (set! (.-innerHTML (.getElementById js/document "compiled-by")) (episode :compiled-by))
    (set! (.-src (.getElementById js/document "audio")) (episode :link))
  ))

(defn init []
  (let [player (get-player)]
    (do
      ; Add theme cycling.
      (let [theme-link (.getElementById js/document "themeLink")]
        (.addEventListener theme-link "click" cycle-theme false))
      ; |  _  |
      ; |=(o)=|    Sienar Fleet Systems'
      ; |     |    TIE/In Space Superiority Starfighter
      (boop)
      (init-compilers data/compilers)
      (init-synopsis (data/episodes 0))
      (init-track (data/episodes 0))
      (init-episodes data/episodes)
      ; Initialize audio player when metadata is loaded.
      (set! (.-onloadedmetadata (@player :audio)) #(init-audio player))
      (.load (@player :audio))
      (loop-tick))))

(.addEventListener js/window "DOMContentLoaded" init)
