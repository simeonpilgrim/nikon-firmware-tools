PROC=fr
ADDITIONAL_GOALS=config

include ../module.mak

config:    $(C)fr.cfg
$(C)fr.cfg: fr.cfg
	$(CP) $? $@


# MAKEDEP dependency list ------------------
$(F)ana$(O)     : $(I)area.hpp $(I)auto.hpp $(I)bitrange.hpp $(I)bytes.hpp   \
	          $(I)diskio.hpp $(I)fpro.h $(I)frame.hpp $(I)funcs.hpp     \
	          $(I)ida.hpp $(I)idp.hpp $(I)kernwin.hpp $(I)lines.hpp     \
	          $(I)llong.hpp $(I)loader.hpp $(I)nalt.hpp $(I)name.hpp    \
	          $(I)netnode.hpp $(I)offset.hpp $(I)pro.h $(I)queue.hpp    \
	          $(I)segment.hpp $(I)ua.hpp $(I)xref.hpp ../idaidp.hpp     \
	          ana.cpp fr.hpp ins.hpp
$(F)emu$(O)     : $(I)area.hpp $(I)auto.hpp $(I)bitrange.hpp $(I)bytes.hpp   \
	          $(I)diskio.hpp $(I)fpro.h $(I)frame.hpp $(I)funcs.hpp     \
	          $(I)ida.hpp $(I)idp.hpp $(I)kernwin.hpp $(I)lines.hpp     \
	          $(I)llong.hpp $(I)loader.hpp $(I)nalt.hpp $(I)name.hpp    \
	          $(I)netnode.hpp $(I)offset.hpp $(I)pro.h $(I)queue.hpp    \
	          $(I)segment.hpp $(I)ua.hpp $(I)xref.hpp ../idaidp.hpp     \
	          emu.cpp fr.hpp ins.hpp
$(F)ins$(O)     : $(I)area.hpp $(I)auto.hpp $(I)bitrange.hpp $(I)bytes.hpp   \
	          $(I)diskio.hpp $(I)fpro.h $(I)frame.hpp $(I)funcs.hpp     \
	          $(I)ida.hpp $(I)idp.hpp $(I)kernwin.hpp $(I)lines.hpp     \
	          $(I)llong.hpp $(I)loader.hpp $(I)nalt.hpp $(I)name.hpp    \
	          $(I)netnode.hpp $(I)offset.hpp $(I)pro.h $(I)queue.hpp    \
	          $(I)segment.hpp $(I)ua.hpp $(I)xref.hpp ../idaidp.hpp     \
	          fr.hpp ins.cpp ins.hpp
$(F)out$(O)     : $(I)area.hpp $(I)auto.hpp $(I)bitrange.hpp $(I)bytes.hpp   \
	          $(I)diskio.hpp $(I)fpro.h $(I)frame.hpp $(I)funcs.hpp     \
	          $(I)ida.hpp $(I)idp.hpp $(I)kernwin.hpp $(I)lines.hpp     \
	          $(I)llong.hpp $(I)loader.hpp $(I)nalt.hpp $(I)name.hpp    \
	          $(I)netnode.hpp $(I)offset.hpp $(I)pro.h $(I)queue.hpp    \
	          $(I)segment.hpp $(I)ua.hpp $(I)xref.hpp ../idaidp.hpp     \
	          fr.hpp ins.hpp out.cpp
$(F)reg$(O)     : $(I)area.hpp $(I)auto.hpp $(I)bitrange.hpp $(I)bytes.hpp   \
	          $(I)diskio.hpp $(I)entry.hpp $(I)fpro.h $(I)frame.hpp     \
	          $(I)funcs.hpp $(I)ida.hpp $(I)idp.hpp $(I)kernwin.hpp     \
	          $(I)lines.hpp $(I)llong.hpp $(I)loader.hpp $(I)nalt.hpp   \
	          $(I)name.hpp $(I)netnode.hpp $(I)offset.hpp $(I)pro.h     \
	          $(I)queue.hpp $(I)segment.hpp $(I)srarea.hpp $(I)ua.hpp   \
	          $(I)xref.hpp ../idaidp.hpp ../iocommon.cpp fr.hpp         \
	          ins.hpp reg.cpp
