(ns utils
  (:require [clojure.string :refer [join]]))

(defn elem [el-id]
  "Gets DOM element by id."
  (.getElementById js/document el-id))

; This does not work; why?
; (defn inner-html [el-id]
;   "Returns innerHTML attribute of the DOM element with el-id."
;   (.-innerHTML (get-elem el-id)))

(defn get-compiler-li [compinfo]
  (str "<li><a href=\"" (compinfo 1) "\">" (compinfo 0) "</a></li>"))

(defn get-synopsis-li [track]
  (str "<li id=\"#\">" (track 0) "</li>"))

(defn get-episode-li [[ep i]]
  (str "<li id=\"ep-" i "\"><b><a href=\"#\">-"
       (if (>= i 10) i (str "0" i))
       ", "
       (ep :title)
       "</a></b> [<i>"
       (join " | " (ep :genres))
       "</i>]</a></li>"))

(defn prepad-0 [x]
  "Convert x to string and add prefix 0 to x if smaller than 10."
  (if (< xs 10) (str "0" xs) xs))

(defn format-hhmmss [duration]
  (let [secs (int duration)
        hours (Math/floor (/ secs 3600))
        minutes (Math/floor (/ (- secs (* hours 3600)) 60))
        seconds (- secs (* hours 3600) (* minutes 60))]
    (str (prepad-0 hours) \: (prepad-0 minutes) \: (prepad-0 seconds))))
