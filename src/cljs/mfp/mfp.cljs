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

(defn init []
  (do
    ; Add theme cycling.
    (let [theme-link (.getElementById js/document "themeLink")]
      (.addEventListener theme-link "click" cycle-theme false))
    ; Initialize text glitch;
    (init-glitch)))

(.addEventListener js/window "DOMContentLoaded" init)

