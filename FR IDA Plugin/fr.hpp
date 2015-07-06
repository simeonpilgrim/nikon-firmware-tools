
#ifndef __FR_HPP
#define __FR_HPP

#define NO_OBSOLETE_FUNCS

#define FR_TYPEINFO_SUPPORT
#define FR_TINFO_SUPPORT

//#define JUMP_DEBUG
//#define FR_TYPE_DEBUG
//#define FR_SWITCH_DEBUG


#include "idaidp.hpp" // "../idaidp.hpp"
#include "ins.hpp"
#include <diskio.hpp>
#include <frame.hpp>
#include <typeinf.hpp>
#include <struct.hpp>


// uncomment this for the final release
//#define __DEBUG__

// FR registers
enum fr_registers {

    // general purpose registers :

    rR0,
    rR1,
    rR2,
    rR3,
    rR4,
    rR5,
    rR6,
    rR7,
    rR8,
    rR9,
    rR10,
    rR11,
    rR12,
    rR13,
    rR14,
    rR15,

    // coprocessor registers :

    rCR0,
    rCR1,
    rCR2,
    rCR3,
    rCR4,
    rCR5,
    rCR6,
    rCR7,
    rCR8,
    rCR9,
    rCR10,
    rCR11,
    rCR12,
    rCR13,
    rCR14,
    rCR15,

    // dedicated registers :

    rPC,        // program counter
    rPS,        // program status
    rTBR,       // table base register
    rRP,        // return pointer
    rSSP,       // system stack pointer
    rUSP,       // user stack pointer
    rMDL,       // multiplication/division register (LOW)
    rMDH,       // multiplication/division register (HIGH)

    // system use dedicated registers
    rReserved6,
    rReserved7,
    rReserved8,
    rReserved9,
    rReserved10,
    rReserved11,
    rReserved12,
    rReserved13,
    rReserved14,
    rReserved15,

    // these 2 registers are required by the IDA kernel :

    rVcs,
    rVds
};


extern const char *const savedRegNames[];

enum fr_phrases {
    fIGR,       // indirect general register
    fIRA,       // indirect relative address
    fIGRP,      // indirect general register with post-increment
    fIGRM,      // indirect general register with pre-decrement
    fR13RI,     // indirect displacement between R13 and a general register
};

// shortcut for a new operand type
#define o_reglist              o_idpspec0

// flags for insn.auxpref
#define INSN_DELAY_SHOT        0x00000001           // postfix insn mnem by ":D"

// flags for opt.specflag1
#define OP_DISPL_IMM_R14       0x00000001           // @(R14, #i)
#define OP_DISPL_IMM_R15       0x00000002           // @(R15, #u)
#define OP_IMM_SIGNED          0x00000020           // IMM signed.
#define OP_ADDR_R              0x00000010           // read-access to memory
#define OP_ADDR_W              0x00000012           // write-access to memory

inline bool op_displ_imm_r14(const op_t &op) { return (op.specflag1 & OP_DISPL_IMM_R14) != 0; }
inline bool op_displ_imm_r15(const op_t &op) { return (op.specflag1 & OP_DISPL_IMM_R15) != 0; }
inline bool op_imm_signed(const op_t &op) { return (op.specflag1 & OP_IMM_SIGNED) != 0; }

// exporting our routines
void idaapi header(void);
void idaapi footer(void);
int idaapi ana(void);
int idaapi emu(void);
void idaapi out(void);
bool idaapi outop(op_t &op);
void idaapi gen_segm_header(ea_t ea);
const ioport_t *find_sym(ea_t address);
bool idaapi create_func_frame(func_t *pfn);
int idaapi get_frame_retsize(func_t *pfn);
int idaapi is_sp_based(const op_t &x);
int idaapi is_align_insn(ea_t ea);

// emu_switch
bool idaapi fr_is_switch(switch_info_ex_t *si);

// emu_type
bool fr_create_lvar(const op_t &x, uval_t v);
bool idaapi can_have_type(op_t &op);
bool calc_fr_arglocs(func_type_data_t *fti);
int use_fr_regarg_type(ea_t ea, const funcargvec_t &rargs);
bool calc_fr_retloc(const tinfo_t &tif, cm_t cc, argloc_t *retloc);
bool is_basic_block_end(void);
void use_fr_arg_types(ea_t ea, func_type_data_t *fti, funcargvec_t *rargs);
int get_fr_fastcall_regs(const int **regs);

extern char device[];

#endif /* __FR_HPP */
