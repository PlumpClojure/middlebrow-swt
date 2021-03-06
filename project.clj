(defproject net.solicode/middlebrow-swt "0.1.0-SNAPSHOT"
  :description "A Middlebrow implementation using SWT's Browser as the backend."
  :url "https://github.com/solicode/middlebrow-swt"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["swt-repo" "https://swt-repo.googlecode.com/svn/repo/"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [net.solicode/middlebrow "0.1.0-SNAPSHOT"]]
  :profiles {:win32.win32.x86
             ^:leaky {:name         "middlebrow-swt.win32.win32.x86"
                      :dependencies [[org.eclipse.swt/org.eclipse.swt.win32.win32.x86 "4.4"]]}
             :win32.win32.x86_64
             ^:leaky {:name         "middlebrow-swt.win32.win32.x86_64"
                      :dependencies [[org.eclipse.swt/org.eclipse.swt.win32.win32.x86_64 "4.4"]]}
             :gtk.linux.x86
             ^:leaky {:name         "middlebrow-swt.gtk.linux.x86"
                      :dependencies [[org.eclipse.swt/org.eclipse.swt.gtk.linux.x86 "4.4"]]}
             :gtk.linux.x86_64
             ^:leaky {:name         "middlebrow-swt.gtk.linux.x86_64"
                      :dependencies [[org.eclipse.swt/org.eclipse.swt.gtk.linux.x86_64 "4.4"]]}
             :cocoa.macosx
             ^:leaky {:name         "middlebrow-swt.cocoa.macosx"
                      :dependencies [[org.eclipse.swt/org.eclipse.swt.cocoa.macosx "4.4"]]}
             :cocoa.macosx.x86_64
             ^:leaky {:name         "middlebrow-swt.cocoa.macosx.x86_64"
                      :dependencies [[org.eclipse.swt/org.eclipse.swt.cocoa.macosx.x86_64 "4.4"]]}})
