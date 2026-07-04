# Makefile — Gestion du Fret Portuaire
# Simplifie la compilation et l'execution de l'application Java Swing.

SRC_DIR     = src
BIN_DIR     = bin
LIB_DIR     = lib
ASSETS_DIR  = assets
MAIN_CLASS  = Main

JAVAC       = javac
JAVA        = java

# Classpath au runtime (bin + jars FlatLaf + assets)
CP          = $(BIN_DIR):$(LIB_DIR)/*:$(ASSETS_DIR)

# Liste de toutes les sources Java
SOURCES     = $(shell find $(SRC_DIR) -name "*.java")

.PHONY: all compile run clean

all: compile

compile:
	@mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) -cp "$(LIB_DIR)/*" $(SOURCES)

run: compile
	$(JAVA) -cp "$(CP)" $(MAIN_CLASS)

clean:
	rm -rf $(BIN_DIR)
