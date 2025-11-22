# Switches for makefile's
JAVAC=javac	# fuer normalen code
#JAVAC=javac -O # -g:none  fuer optimierten code
JAR = jar -cvf jcself.jar # create archive
JAVATOP= $(JDKHOME)
CCDYN = $(CC) -I$(JAVATOP)/include -I$(JAVATOP)/include/win32 -MD -c
DYNLDOPTS = -LD -Fe$@
LDDYN = $(CC) $(DYNLDOPTS)

.SUFFIXES: .class .java .j

.java.class:
	$(JAVAC) $<

.j.java:
	$(CPP) $<

.j.class:
	$(CPP) $<
	$(JAVAC) $*.java



