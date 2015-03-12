(defonce os
  (let [os-name (.toLowerCase (System/getProperty "os.name" "") java.util.Locale/ENGLISH)]
    (cond
      (or (.contains os-name "mac") (.contains os-name "darwin"))
      :mac
      (.contains os-name "win")
      :windows
      (.contains os-name "linux")
      :linux
      :else
      :unknown)))

(defn os? [target-os]
  (= os target-os))

(defn swt-arch []
  (if (.contains (System/getProperty "os.arch" "") "64")
    "x86_64"
    "x86"))

(defn swt-windowing-library []
  (case os
    :linux "gtk"
    :mac "cocoa"
    :windows "win32"))

(defn swt-platform []
  (case os
    :linux "linux"
    :mac "macosx"
    :windows "win32"))

; TODO: Determining the dependency dynamically like this is fine for development, but
; it's not going to work for deployment.
(defn swt-dependency [swt-version]
  [(symbol "org.eclipse.swt"
     (str "org.eclipse.swt." (swt-windowing-library) \. (swt-platform) \. (swt-arch)))
   swt-version])

(defproject net.solicode/middlebrow-swt "0.1.0-SNAPSHOT"
  :description "A Middlebrow implementation using SWT's Browser as the backend."
  :url "https://github.com/solicode/middlebrow-swt"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ~(if (os? :mac) ["-XstartOnFirstThread"] [])
  :repositories [["swt-repo" "https://swt-repo.googlecode.com/svn/repo/"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [net.solicode/middlebrow "0.1.0-SNAPSHOT"]
                 ~(swt-dependency "4.4")])
