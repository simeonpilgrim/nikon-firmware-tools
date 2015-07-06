
#include "fr.hpp"

// Output a register
static void out_reg(ushort reg)
{
  out_register(ph.regNames[reg]);
}

// Output an operand as a register
static void out_reg(const op_t &op)
{
  out_reg(op.reg);
}

// Output an operand as an immediate value
static void out_imm(const op_t &op, bool no_sharp = false)
{
  if ( !no_sharp )
    out_symbol('#');
  OutValue(op, OOFW_IMM);
}

// Output an operand as an address
static void out_addr(const op_t &op)
{
  if ( !out_name_expr(op, toEA(cmd.cs, op.addr), op.addr) )
    OutValue(op, OOF_ADDR | OOFS_NOSIGN);
}

static bool print_comma = false;
static void out_reg_if_bit(ushort reg, uval_t value, int bit)
{
  if ( (value & bit) == bit )
  {
    if ( print_comma )
    {
      out_symbol(',');
      OutChar(' ');
    }
    out_reg(reg);
    print_comma = true;
  }
}

static void out_reglist(const op_t &op)
{
  static const uint16 regs_ldm0[] = { rR7,  rR6,  rR5,  rR4,  rR3,  rR2,  rR1,  rR0  };
  static const uint16 regs_stm0[] = { rR0,  rR1,  rR2,  rR3,  rR4,  rR5,  rR6,  rR7  };
  static const uint16 regs_ldm1[] = { rR15, rR14, rR13, rR12, rR11, rR10, rR9,  rR8  };
  static const uint16 regs_stm1[] = { rR8,  rR9,  rR10, rR11, rR12, rR13, rR14, rR15 };
  const uint16 *regs;
  bool left;

  switch ( cmd.itype )
  {
    case fr_ldm0:   regs = regs_ldm0; left = false; break;
    case fr_stm0:   regs = regs_stm0; left = true;  break;
    case fr_ldm1:   regs = regs_ldm1; left = false; break;
    case fr_stm1:   regs = regs_stm1; left = true;  break;
    default:
      INTERR(10018);
  }

  print_comma = false;

  out_symbol('(');
  if ( left )
  {
    for (int i = 0, bit = 128; bit != 0; bit >>= 1, i++)
      out_reg_if_bit(regs[i], op.value, bit);
  }
  else
  {
    for (int i = 7, bit = 1; bit <= 128; bit <<= 1, i--)
      out_reg_if_bit(regs[i], op.value, bit);
  }
  out_symbol(')');
}

// Generate disassembly header
void idaapi header(void)
{
  gen_header(GH_PRINT_ALL_BUT_BYTESEX, NULL, device);
}

// Generate disassembly footer
void idaapi footer(void)
{
  char buf[MAXSTR];
  char *const end = buf + sizeof(buf);
  if ( ash.end != NULL )
  {
    MakeNull();
    qstring name;
    char *p = tag_addstr(buf, end, COLOR_ASMDIR, ash.end);
    if ( get_colored_name(&name, inf.beginEA) > 0 )
    {
      APPCHAR(p, end, ' ');
      APPEND(p, end, name.begin());
    }
    MakeLine(buf, inf.indent);
  }
  else
  {
    gen_cmt_line("end of file");
  }
}

// Generate a segment header
void idaapi gen_segm_header(ea_t ea)
{
  char sname[MAXNAMELEN];
  segment_t *Sarea = getseg(ea);
  if ( get_segm_name(Sarea, sname, sizeof(sname)) <= 0 )
    return;

  char *segname = sname;
  if ( *segname == '_' )
    segname++;

  printf_line(inf.indent, COLSTR(".section .%s", SCOLOR_ASMDIR), segname);

  ea_t orgbase = ea - get_segm_para(Sarea);

  if ( orgbase != 0 )
  {
    char buf[MAX_NUMBUF];
    btoa(buf, sizeof(buf), orgbase);
    printf_line(inf.indent, COLSTR("%s %s", SCOLOR_ASMDIR), ash.origin, buf);
  }
}

// Output an operand.
bool idaapi outop(op_t &op)
{
  switch ( op.type )
  {
    case o_near:
    case o_mem:
      out_addr(op);
      break;

    // immediate value
    case o_imm:
      {
        const ioport_t *port = find_sym(op.value);

        // this immediate is represented in the .cfg file
        // output the port name instead of the numeric value
        if ( port != NULL )
          out_line(port->name, COLOR_IMPNAME);
        else // otherwise, simply print the value
        {
          out_symbol('#');
          int flags = OOFW_IMM;
          if( op_imm_signed(op) )
          {
            flags |= OOF_SIGNED;
          }

          OutValue(op, flags );
        }
      }
      break;

    // register
    case o_reg:
      out_reg(op);
      break;

    // phrase
    case o_phrase:
      out_symbol('@');
      switch ( op.specflag2 )
      {
        case fIGR:       // indirect general register
          out_reg(op);
          break;

        case fIRA:       // indirect relative address
          OutValue(op, OOF_ADDR | OOFS_NOSIGN);
          break;

        case fIGRP:      // indirect general register with post-increment
          out_reg(op);
          out_symbol('+');
          break;

        case fIGRM:      // indirect general register with pre-decrement
          out_symbol('-');
          out_reg(op);
          break;

        case fR13RI:     // indirect displacement between R13 and a general register
          out_symbol('(');
          out_reg(rR13);
          out_symbol(',');
          OutChar(' ');
          out_reg(op);
          out_symbol(')');
          break;

        default:
          INTERR(10019);
      }
      break;

    // displacement
    case o_displ:
      out_symbol('@');
      out_symbol('(');

      // @(R14, #i)
      if ( op_displ_imm_r14(op) )
      {
        out_reg(rR14);
        out_symbol(',');
        OutChar(' ');
        OutValue(op, OOFW_IMM | OOF_SIGNED);
      }
      // @(R15, #i)
      else if ( op_displ_imm_r15(op) )
      {
        out_reg(rR15);
        out_symbol(',');
        OutChar(' ');
        OutValue(op, OOFW_IMM );
      }
      else
        INTERR(10020);

      out_symbol(')');
      break;

    // reglist
    case o_reglist:
      out_reglist(op);
      break;

    // void operand
    case o_void:
      break;

    default:
      INTERR(10021);
  }
  return 1;
}

// Output an instruction
void idaapi out(void)
{

  //
  // print insn mnemonic
  //

  char buf[MAXSTR];
  init_output_buffer(buf, sizeof(buf));

  char postfix[5];
  postfix[0] = '\0';

  if ( cmd.auxpref & INSN_DELAY_SHOT )
  {
    qstrncpy(postfix, ":D", sizeof(postfix));
  }
  OutMnem(8, postfix);

  for ( int i=0; i < 4; i++ )
  {
    if ( cmd.Operands[i].type != o_void )
    {
      if ( i != 0 )
      {
        out_symbol(',');
        OutChar(' ');
      }
      out_one_operand(i);
    }
  }

  // output a character representation of the immediate values
  // embedded in the instruction as comments
  if ( isVoid(cmd.ea,uFlag,0)) OutImmChar(cmd.Op1 );
  if ( isVoid(cmd.ea,uFlag,1)) OutImmChar(cmd.Op2 );
  if ( isVoid(cmd.ea,uFlag,2)) OutImmChar(cmd.Op3 );
  if ( isVoid(cmd.ea,uFlag,3)) OutImmChar(cmd.Op4 );

  term_output_buffer();                   // terminate the output string
  gl_comm = 1;                            // ask to attach a possible user-
                                          // defined comment to it
  MakeLine(buf);                          // pass the generated line to the
                                          // kernel
}
