U=$(USER)
TYPE=png
GABLOCK=gablock-code/gablock
DEBUG=-d

all: d90.$(TYPE) d90_base.$(TYPE)

%.$(TYPE): %.yaml gablock-code
	$(GABLOCK) -t $(TYPE) -o $@ $(DEBUG) $<

checkout:
	hg clone ssh://$(U)@hg.code.sf.net/p/gablock/code gablock-code 
