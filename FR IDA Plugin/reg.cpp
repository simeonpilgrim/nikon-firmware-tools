
#include "fr.hpp"
#include <srarea.hpp>

//#define IDA65

// The netnode helper.
// Using this node we will save current configuration information in the
// IDA database.
static netnode helper;

// FR registers names
static const char *const RegNames[] =
{
  // general purpose registers :

  "r0",
  "r1",
  "r2",
  "r3",
  "r4",
  "r5",
  "r6",
  "r7",
  "r8",
  "r9",
  "r10",
  "r11",
  "r12",
  "r13",
  "r14",
  "r15",

  // coprocessor registers :

  "cr0",
  "cr1",
  "cr2",
  "cr3",
  "cr4",
  "cr5",
  "cr6",
  "cr7",
  "cr8",
  "cr9",
  "cr10",
  "cr11",
  "cr12",
  "cr13",
  "cr14",
  "cr15",

  // dedicated registers :

  "pc",        // program counter
  "ps",        // program status
  "tbr",       // table base register
  "rp",        // return pointer
  "ssp",       // system stack pointer
  "usp",       // user stack pointer
  "mdl",       // multiplication/division register (LOW)
  "mdh",       // multiplication/division register (HIGH)

  // system use dedicated registers
  "reserved6",
  "reserved7",
  "reserved8",
  "reserved9",
  "reserved10",
  "reserved11",
  "reserved12",
  "reserved13",
  "reserved14",
  "reserved15",

  // these 2 registers are required by the IDA kernel :

  "cs",
  "ds"
};

const char *const savedRegNames[] =
{
  // general purpose registers :

  "sR0",
  "sR1",
  "sR2",
  "sR3",
  "sR4",
  "sR5",
  "sR6",
  "sR7",
  "sR8",
  "sR9",
  "sR10",
  "sR11",
  "sR12",
  "sR13",
  "sR14",
  "sR15",

  // coprocessor registers :

  "sCr0",
  "sCcr1",
  "sCcr2",
  "sCcr3",
  "sCcr4",
  "sCcr5",
  "sCcr6",
  "sCcr7",
  "sCcr8",
  "sCcr9",
  "sCcr10",
  "sCcr11",
  "sCcr12",
  "sCcr13",
  "sCcr14",
  "sCcr15",

  // dedicated registers :

  "sPc",        // program counter
  "sPs",        // program status
  "sTbr",       // table base register
  "sRp",        // return pointer
  "sSsp",       // system stack pointer
  "sSp",       // user stack pointer
  "sMdl",       // multiplication/division register (LOW)
  "sMdh",       // multiplication/division register (HIGH)

  // system use dedicated registers
  "reserved6",
  "reserved7",
  "reserved8",
  "reserved9",
  "reserved10",
  "reserved11",
  "reserved12",
  "reserved13",
  "reserved14",
  "reserved15",

  // these 2 registers are required by the IDA kernel :

  "cs",
  "ds"
};

static size_t numports = 0;
static ioport_t *ports = NULL;
char device[MAXSTR] = "";

// include IO common routines (such as set_device_name, apply_config_file, etc..)
#include "iocommon.cpp" // "../iocommon.cpp"

inline static void idaapi choose_device(TView *[] = NULL, int = 0)
{
  char cfgfile[QMAXFILE];
  get_cfg_filename(cfgfile, sizeof(cfgfile));
  if ( choose_ioport_device(cfgfile, device, sizeof(device), NULL) )
    set_device_name(device, IORESP_NONE);
}

// returns a pointer to a ioport_t object if address was found in the config file.
// otherwise, returns NULL.
const ioport_t *find_sym(ea_t address)
{
  return find_ioport(ports, numports, address);
}

// The kernel event notifications
// Here you may take desired actions upon some kernel events
static int idaapi notify(processor_t::idp_notify msgid, ...)
{
	va_list va;
	va_start(va, msgid);

	// A well behavior processor module should call invoke_callbacks()
	// in his notify() function. If this function returns 0, then
	// the processor module should process the notification itself
	// Otherwise the code should be returned to the caller:

	int code = invoke_callbacks(HT_IDP, msgid, va);
	if ( code ) return code;

	switch ( msgid )
	{
	case processor_t::init:
		inf.mf = 1;
		helper.create("$ fr");
	default:
		break;

	case processor_t::term:
		free_ioports(ports, numports);
		break;

	case processor_t::newfile:
		choose_device();
		set_device_name(device, IORESP_ALL);
		break;

	case processor_t::oldfile:
		{
			char buf[MAXSTR];
			if ( helper.supval(-1, buf, sizeof(buf)) > 0 )
				set_device_name(buf, IORESP_NONE);
		}
		break;

	case processor_t::closebase:
	case processor_t::savebase:
		helper.supset(-1, device);
		break;

	case processor_t::is_basic_block_end:
		return is_basic_block_end() ? 2 : 0;


#ifdef FR_TYPEINFO_SUPPORT
		// +++ TYPE CALLBACKS
	case processor_t::max_ptr_size:
		return 4+1;

	case processor_t::get_default_enum_size: // get default enum size
		// args:  cm_t cm
		// returns: sizeof(enum)
		{
			//        cm_t cm        =  va_argi(va, cm_t);
			return 1; // inf.cc.size_e;
		}

	case processor_t::based_ptr:
		{
			uint ptrt      = va_arg(va, unsigned int); qnotused(ptrt);
			char **ptrname = va_arg(va, char **);
			*ptrname = NULL;
			return 0;                       // returns: size of type
		}

	case processor_t::get_stkarg_offset2:
		// get offset from SP to the first stack argument
		// args: none
		// returns: the offset+2
		return 0x00 + 2;

	case processor_t::calc_cdecl_purged_bytes2:
		// calculate number of purged bytes after call
		{
			//ea_t ea = va_arg(va, ea_t);
			return 0x00 + 2;
		}

#endif // FR_TYPEINFO_SUPPORT 
#ifdef FR_TINFO_SUPPORT
	case processor_t::decorate_name3:
		{
			qstring *outbuf  = va_arg(va, qstring *);
			const char *name = va_arg(va, const char *);
			bool mangle      = va_argi(va, bool);
			cm_t cc          = va_argi(va, cm_t);
			return gen_decorate_name3(outbuf, name, mangle, cc) ? 2 : 0;
		}

	case processor_t::calc_retloc3:
		//msg("calc_retloc3\n");
		{
			const tinfo_t *type = va_arg(va, const tinfo_t *);
			cm_t cc             = va_argi(va, cm_t);
			argloc_t *retloc    = va_arg(va, argloc_t *);
			return calc_fr_retloc(*type, cc, retloc) ? 2 : -1;
		}
		break;  

	case processor_t::calc_varglocs3:
		return 1; // not implemented
		break;

	case processor_t::calc_arglocs3:
		{
			//msg("calc_arglocs3\n");
			func_type_data_t *fti = va_arg(va, func_type_data_t *);
			return calc_fr_arglocs(fti) ? 2 : -1;
		}

	case processor_t::use_stkarg_type3:
		{
			//msg("use_stkarg_type3\n");
			ea_t ea               = va_arg(va, ea_t);
			const funcarg_t *arg  = va_arg(va, const funcarg_t* );
		}
		return false;
		break;

	case processor_t::use_regarg_type3:
		//msg("use_regarg_type3\n");
		{
			int *used                 = va_arg(va, int *);
			ea_t ea                   = va_arg(va, ea_t);
			const funcargvec_t *rargs = va_arg(va, const funcargvec_t *);
			*used = use_fr_regarg_type(ea, *rargs);
			return 2;
		}
		break;

	case processor_t::use_arg_types3:
		{
			ea_t ea               = va_arg(va, ea_t);
			func_type_data_t *fti = va_arg(va, func_type_data_t *);
			funcargvec_t *rargs   = va_arg(va, funcargvec_t *);
			use_fr_arg_types(ea, fti, rargs);
			return 2;
		}
#ifdef IDA65
	case processor_t::get_fastcall_regs2:
	case processor_t::get_varcall_regs2:
		{
			const int **regs = va_arg(va, const int **);
			return get_fr_fastcall_regs(regs) + 2;
		}

	case processor_t::get_thiscall_regs2:
		{
			const int **regs = va_arg(va, const int **);
			*regs = NULL;
			return 2;
		}
#else
	case processor_t::get_fastcall_regs3:
	case processor_t::get_varcall_regs3:
		{
			const int *regs;
			get_fr_fastcall_regs(&regs);
			callregs_t *callregs = va_arg(va, callregs_t *);
			callregs->set(ARGREGS_INDEPENDENT, regs, NULL);
			return callregs->nregs + 2;
		}

	case processor_t::get_thiscall_regs3:
		{
			callregs_t *callregs = va_arg(va, callregs_t *);
			callregs->reset();
			return 2;
		}
#endif IDA65
#endif // FR_TINFO_SUPPORT
	}

    va_end(va);

    return(1);
}

const char *idaapi set_idp_options(
    const char *keyword,
    int /*value_type*/,
    const void * /*value*/ )
{
    if ( keyword != NULL )
        return IDPOPT_BADKEY;

    char cfgfile[QMAXFILE];
    get_cfg_filename(cfgfile, sizeof(cfgfile));
    if ( !choose_ioport_device(cfgfile, device, sizeof(device), NULL)
      && strcmp(device, NONEPROC) == 0 )
    {
      warning("No devices are defined in the configuration file %s", cfgfile);
    }
    else
    {
      set_device_name(device, IORESP_NONE);
    }
    return IDPOPT_OK;
}


//
//  GNU assembler for fujitsu FR
//

// gets a function's name
//lint -e{818} could be declared const
static bool fr_get_func_name(qstring *name, func_t *pfn)
{
  ea_t ea = pfn->startEA;
  if ( get_demangled_name(name, ea, inf.long_demnames, DEMNAM_NAME) <= 0 )
    return false;

  char tag[2+COLOR_ADDR_SIZE+1];
  *tag_addr(tag, tag+sizeof(tag), ea) = '\0';
  name->insert(tag);
  return true;
}

// prints function header
static void idaapi gnu_func_header(func_t *pfn)
{
  std_gen_func_header(pfn);

  qstring namebuf;
  if ( fr_get_func_name(&namebuf, pfn) )
  {
    const char *name = namebuf.begin();
    if ( is_public_name(pfn->startEA) && ash.a_public != NULL )
      printf_line(inf.indent, COLSTR("%s %s", SCOLOR_ASMDIR), ash.a_public, name);
    printf_line(inf.indent, COLSTR(".type %s, @function", SCOLOR_ASMDIR), name);
    printf_line(0, COLSTR("%s:", SCOLOR_ASMDIR), name);
  }
}

// prints function footer
static void idaapi gnu_func_footer(func_t *pfn)
{
  qstring namebuf;
  if ( fr_get_func_name(&namebuf, pfn) )
  {
    const char *name = namebuf.begin();
    printf_line(inf.indent, COLSTR(".size %s, .-%s", SCOLOR_ASMDIR), name, name);
  }
}

static const asm_t gnu_asm =
{
  AS_COLON |
  ASH_HEXF3 |   // hex 0x123 format
  ASB_BINF0 |   // bin 0110b format
  ASO_OCTF1 |   // oct 012345 format
  // don't display the final 0 in string declarations
  /*AS_1TEXT |*/ AS_NCMAS,
  0,
  "GNU Assembler for the Fujitsu FR Family",
  0,
  NULL,         // no headers
  NULL,         // no bad instructions
  ".org",       // origin directive
  NULL,         // end directive
  ";",          // comment string
  '"',          // string delimiter
  '\'',         // char delimiter
  "\\\"'",      // special symbols in char and string constants
  ".ascii",     // ascii string directive
  ".byte",      // byte directive
  ".word",      // word directive
  ".long",      // dword  (4 bytes)
  NULL,         // qword  (8 bytes)
  NULL,         // oword  (16 bytes)
  ".float",     // float  (4 bytes)
  ".double",    // double (8 bytes)
  NULL,         // tbyte  (10/12 bytes)
  NULL,         // packed decimal real
  NULL,         // arrays (#h,#d,#v,#s(...)
  "dfs %s",     // uninited arrays
  "equ",        // Equ
  NULL,         // seg prefix
  NULL,         // checkarg_preline()
  NULL,         // checkarg_atomprefix()
  NULL,         // checkarg_operations()
  NULL,         // translation to use in character & string constants
  "$",          // current IP (instruction pointer) symbol in assembler
  gnu_func_header,     // func_header
  gnu_func_footer,     // func_footer
  ".globl",     // public
  NULL,         // weak
  NULL,         // extrn
  NULL,         // comm
  NULL,         // get_type_name
  ".align",     // align
  '(', ')',     // lbrace, rbrace
  "%",          // mod
  "&",          // and
  "|",          // or
  "^",          // xor
  "!",          // not
  "<<",         // shl
  ">>",         // shr
  NULL,         // sizeof
  0,            // flag2 ???
  NULL,         // comment close string
  NULL,         // low8 op
  NULL,         // high8 op
  NULL,         // low16 op
  NULL          // high16 op
};

//
// Supported assemblers :
//

static const asm_t *const asms[] = { &gnu_asm, NULL };

//
// Short and long name for our module
//
#define FAMILY "Fujitsu FR 32-Bit Family:"

static const char *const shnames[] =
{
  "fr",
  NULL
};

static const char *const lnames[] =
{
  FAMILY"Fujitsu FR 32-Bit Family",
  NULL
};

static const uchar retcode_1[] = { 0x97, 0x20 };    // ret
static const uchar retcode_2[] = { 0x9F, 0x20 };    // ret with delay shot
static const uchar retcode_3[] = { 0x9F, 0x30 };    // reti

static const bytes_t retcodes[] =
{
  { sizeof(retcode_1), retcode_1 },
  { sizeof(retcode_2), retcode_2 },
  { sizeof(retcode_3), retcode_3 },
  { 0, NULL }                            // NULL terminated array
};

//-----------------------------------------------------------------------
//      Processor Definition
//-----------------------------------------------------------------------
processor_t LPH =
{
      IDP_INTERFACE_VERSION,// version
      PLFM_FR,              // id
      PR_RNAMESOK           // can use register names for byte names
      | PR_USE32            // 32-bit processor
      | PR_DEFSEG32         // create 32-bit segments by default
#ifdef FR_TYPEINFO_SUPPORT
      | PR_TYPEINFO         // Support the simple type system notifications
#endif // FR_TYPEINFO_SUPPORT
#ifdef FR_TINFO_SUPPORT
      | PR_TINFO            // Support the complex type system notifications
      | PR_USE_ARG_TYPES
#endif // FR_TINFO_SUPPORT
      | PR_DELAYED
      | PR_BINMEM,          // The module creates RAM/ROM segments for binary files
                            // (the kernel shouldn't ask the user about their sizes and addresses)
      8,                    // 8 bits in a byte for code segments
      8,                    // 8 bits in a byte for other segments

      shnames,              // array of short processor names
                            // the short names are used to specify the processor
                            // with the -p command line switch)
      lnames,               // array of long processor names
                            // the long names are used to build the processor type
                            // selection menu

      asms,                 // array of target assemblers

      notify,               // the kernel event notification callback

      header,               // generate the disassembly header
      footer,               // generate the disassembly footer

      gen_segm_header,      // generate a segment declaration (start of segment)
      std_gen_segm_footer,  // generate a segment footer (end of segment)

      NULL,                 // generate 'assume' directives

      ana,                  // analyze an instruction and fill the 'cmd' structure
      emu,                  // emulate an instruction

      out,                  // generate a text representation of an instruction
      outop,                // generate a text representation of an operand
      intel_data,           // generate a text representation of a data item
      NULL,                 // compare operands
      can_have_type,        // can an operand have a type?

      qnumber(RegNames),    // Number of registers
      RegNames,             // Register names
      NULL,                 // get abstract register

      0,                    // Number of register files
      NULL,                 // Register file names
      NULL,                 // Register descriptions
      NULL,                 // Pointer to CPU registers

      rVcs, rVds,
      0,                    // size of a segment register
      rVcs, rVds,

      NULL,                 // No known code start sequences
      retcodes,

      0, fr_last,
      Instructions,

      NULL,                 // int  (*is_far_jump)(int icode);
      NULL,                 // Translation function for offsets
      0,                    // int tbyte_size;  -- doesn't exist
      NULL,                 // int (*realcvt)(void *m, ushort *e, ushort swt);
      { 0, 7, 15, 0 },      // char real_width[4];
                            // number of symbols after decimal point
                            // 2byte float (0-does not exist)
                            // normal float
                            // normal double
                            // long double
      fr_is_switch,         // int (*is_switch)(switch_info_t *si);
      NULL,                 // int32 (*gen_map_file)(FILE *fp);
      NULL,                 // ea_t (*extract_address)(ea_t ea,const char *string,int x);
      is_sp_based,          // int (*is_sp_based)(op_t &x);
      create_func_frame,    // int (*create_func_frame)(func_t *pfn);
      get_frame_retsize,    // int (*get_frame_retsize(func_t *pfn)
      NULL,                 // void (*gen_stkvar_def)(char *buf,const member_t *mptr,int32 v);
      gen_spcdef,           // Generate text representation of an item in a special segment
      fr_ret,               // Icode of return instruction. It is ok to give any of possible return instructions
      set_idp_options,      // const char *(*set_idp_options)(const char *keyword,int value_type,const void *value);
      is_align_insn,        // int (*is_align_insn)(ea_t ea);
      NULL                  // mvm_t *mvm;
};
