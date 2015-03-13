(ns middlebrow-swt.core
  (:require [clojure.java.io :as io]
            [middlebrow.core :refer :all]
            [middlebrow.util :refer :all])
  (:import [org.eclipse.swt SWT]
           [org.eclipse.swt.browser Browser]
           [org.eclipse.swt.layout GridData GridLayout]
           [org.eclipse.swt.widgets Shell Display Listener]
           [org.eclipse.swt.graphics Image]))

(defmacro run-async [& body]
  `(-> (Display/getDefault) (.asyncExec (fn [] ~@body))))

(defmacro run-sync [& body]
  `(let [p# (promise)]
     (-> (Display/getDefault) (.asyncExec #(deliver p# (do ~@body))))
     @p#))

(defmacro call [call-type state & body]
  `(case ~call-type
     :direct ~@body
     ; Executing synchronous operations when the UI loop hasn't yet started would cause a deadlock
     ; as it would be impossible for SWT to receive and process it. Hence we would be waiting
     ; forever for it to complete. Therefore, if the loop hasn't yet started, simply use a direct
     ; call rather than a syncherous one.
     :sync (if (:loop-started (deref ~state))
             (run-sync ~@body)
             ~@body)
     :async (run-async ~@body)))

(defprotocol ISWTBrowser
  (start-ui-loop [self] [self error-fn]))

(defrecord SWTBrowser [shell browser call-type state]
  IBrowser
  (show [self]
    (call call-type state
      (.open shell)))

  (hide [self]
    (call call-type state
      (.setVisible shell false)))

  (activate [self]
    (call call-type state
      (.forceActive shell)))

  (deactivate [self]
    ; TODO: SWT doesn't seem to support deactivating windows. We may have to find a way to implement this ourselves.
    (throw (UnsupportedOperationException. "Deactivating windows is not currently supported for SWTBrowser.")))

  (close [self]
    (call call-type state
      (.close shell)))

  (visible? [self]
    (call call-type state
      (.isVisible shell)))

  (minimize [self]
    (call call-type state
      (.setMinimized shell true)))

  (maximize [self]
    (call call-type state
      (.setMinimized shell false)
      (.setMaximized shell true)))

  (minimized? [self]
    (call call-type state
      (.getMinimized shell)))

  (maximized? [self]
    (call call-type state
      (.getMaximized shell)))

  (set-fullscreen [self fullscreen]
    (call call-type state
      (.setFullScreen shell fullscreen)))

  (fullscreen? [self]
    (call call-type state
      (.getFullScreen shell)))

  (get-title [self]
    (call call-type state
      (.getText shell)))

  (set-title [self title]
    (call call-type state
      (.setText shell title)))

  (get-x [self]
    (call call-type state
      (-> (.getLocation shell) .x)))

  (set-x [self x]
    (call call-type state
      (.setLocation shell x (get-y self))))

  (get-y [self]
    (call call-type state
      (-> (.getLocation shell) .y)))

  (set-y [self y]
    (call call-type state
      (.setLocation shell (get-x self) y)))

  (get-position [self]
    (let [position (call call-type state (.getLocation shell))]
      [(.x position) (.y position)]))

  (set-position [self position]
    (let [[x y] position]
      (call call-type state
        (.setLocation shell x y))))

  (set-position [self x y]
    (call call-type state
      (.setLocation shell x y)))

  (get-width [self]
    (call call-type state
      (-> (.getSize shell) .x)))

  (set-width [self width]
    (let [height (get-height self)]
      (call call-type state
        (.setSize shell width height))))

  (get-height [self]
    (-> (call call-type state (.getSize shell)) .y))

  (set-height [self height]
    (let [width (get-width self)]
      (call call-type state
        (.setSize shell width height))))

  (get-size [self]
    (let [dimensions (call call-type state (.getSize shell))]
      [(.x dimensions) (.y dimensions)]))

  (set-size [self dimensions]
    (let [[width height] dimensions]
      (call call-type state
        (.setSize shell width height))))

  (set-size [self width height]
    (call call-type state
      (.setSize shell width height)))

  (get-url [self]
    (call call-type state
      (.getUrl browser)))

  (set-url [self url]
    (call call-type state
      (.setUrl browser url)))

  (listen-closed [self handler]
    (call call-type state
      (.addListener shell SWT/Close
        (proxy [Listener] []
          (handleEvent [e]
            (handler {:event e}))))))

  (listen-focus-gained [self handler]
    (call call-type state
      (.addListener shell SWT/Activate
        (proxy [Listener] []
          (handleEvent [e]
            (handler {:event e}))))))

  (listen-focus-lost [self handler]
    (call call-type state
      (.addListener shell SWT/Deactivate
        (proxy [Listener] []
          (handleEvent [e]
            (handler {:event e}))))))

  (container-type [self] :swt)

  (start-event-loop [self]
    (start-event-loop self nil))

  (start-event-loop [self error-fn]
    (swap! state assoc :loop-started true)
    (let [display (Display/getDefault)]
      (while (not (.isDisposed shell))
        (try
          (if-not (.readAndDispatch display)
            (.sleep display))
          (catch Exception e
            (if error-fn
              (error-fn e)
              (println "Unhandled exception:" (throwable->string e))))))
      (.dispose display))))

(defn style->swt-style [style]
  (case style
    nil SWT/SHELL_TRIM
    :normal SWT/SHELL_TRIM
    :undecorated SWT/NO_TRIM
    :tool (bit-or SWT/SHELL_TRIM SWT/TOOL)
    style))

(defn engine->swt-engine [engine]
  (case engine
    nil SWT/NONE
    :default SWT/NONE
    :webkit SWT/WEBKIT
    :mozilla SWT/MOZILLA
    engine))

(defn create-window [& {:keys [title url x y width height style engine icons call-type]
                        :as   opts}]
  (let [display (Display.)
        shell (Shell. display (style->swt-style style))
        main-layout (GridLayout. 1 false)]
    (doto shell
      (.setText (or title "Middlebrow"))
      (.setSize (or width 400) (or height 300)))

    (set-fields! main-layout
      marginWidth 0
      marginHeight 0
      horizontalSpacing 0
      verticalSpacing 0
      marginLeft 0
      marginTop 0
      marginRight 0
      marginBottom 0)

    (.setLayout shell main-layout)

    ; TODO: Add option to set IME input mode
    ;(.setImeInputMode shell SWT/NATIVE)

    ; TODO: Create default Middlebrow icons?
    ;(let [icons (or icons ["images/icon-128.png"
    ;                       "images/icon-32.png"
    ;                       "images/icon-16.png"])]
    (when icons
      (.setImages shell
        (into-array
          (map
            #(Image. display (io/input-stream (io/resource %)))
            icons))))

    (when (or x y)
      (.setLocation shell (or x 0) (or y 0)))

    (let [browser (Browser. shell (engine->swt-engine engine))]
      (.setLayoutData browser (GridData. SWT/FILL SWT/FILL true true 1 1))
      (.setUrl browser (or url "about:blank"))
      (->SWTBrowser shell browser (or call-type :sync) (atom {:loop-started false})))))
