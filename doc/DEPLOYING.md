Deploying
=========

Since SWT has different dependency requirements depending on the OS and architecture that is being targeted, multiple artifacts need to be created. This is done using profiles in Leiningen.

The full deploy command is as follows:

#### Local Repository

```
lein do \
  with-profile win32.win32.x86 install, \
  with-profile win32.win32.x86_64 install, \
  with-profile gtk.linux.x86 install, \
  with-profile gtk.linux.x86_64 install, \
  with-profile cocoa.macosx install, \
  with-profile cocoa.macosx.x86_64 install  
```

#### Clojars

```
lein do \
  with-profile win32.win32.x86 deploy clojars, \
  with-profile win32.win32.x86_64 deploy clojars, \
  with-profile gtk.linux.x86 deploy clojars, \
  with-profile gtk.linux.x86_64 deploy clojars, \
  with-profile cocoa.macosx deploy clojars, \
  with-profile cocoa.macosx.x86_64 deploy clojars  
```