
#include "fr.hpp"
#include "emu_search.h"

// Analyze an instruction
static ea_t next_insn(ea_t ea)
{
  if ( decode_insn(ea) == 0 )
    return 0;
  ea += cmd.size;
  return ea;
}

// Emulate an operand.
static void handle_operand(const op_t &op)
{
  bool offset = false;
  switch ( op.type )
  {
    case o_near:
      //msg("0x%a handle_operand o_near ua_add_cref: from: %a to:0x%a\n", cmd.ea, op.offb, toEA(cmd.cs, op.addr));
      ua_add_cref(op.offb, toEA(cmd.cs, op.addr), (cmd.itype == fr_call) ? fl_CN : fl_JN);
      break;

    case o_mem:
      {
          enum dref_t mode = dr_U;

          if ( op.specflag1 & OP_ADDR_R )           mode = dr_R;
          else if ( op.specflag1 & OP_ADDR_W )      mode = dr_W;

          ea_t ea = toEA(cmd.cs, op.addr);
          ua_add_dref(op.offb, ea, mode);
          ua_dodata2(op.offb, ea, op.dtyp);
      }
      break;

    case o_imm:
      // if current insn is ldi:32 #imm, r1
      // and next insn is call @r1,
      // replace the immediate value with an offset.
      if (cmd.itype == fr_ldi_32 &&
          cmd.Op1.type == o_imm &&
          cmd.Op2.type == o_reg)
      {
        const int callreg = cmd.Op2.reg;
        insn_t cmd_backup = cmd;
        if ( next_insn(cmd.ea + cmd.size ) > 0
          && ( cmd.itype == fr_call || cmd.itype == fr_jmp )
          && cmd.Op1.type == o_phrase
          && cmd.Op1.specflag2 == fIGR
          && cmd.Op1.reg == callreg )
        {
          offset = true;
        }
        cmd = cmd_backup;
        if( offset )
        {
          //set_offset(cmd_backup.ea, 0, 0);
        }
      }
      doImmd(cmd.ea);
      // if the value was converted to an offset, then create a data xref:
      if ( !offset && op_adds_xrefs(uFlag, op.n) )
        ua_add_off_drefs2(op, dr_O, 0);

      // create stack variables if necessary
      {
        bool ok = false;
        // ldi8 #our_value, R1
        // extsb R1
        // addn R14, R1
        if ( cmd.itype == fr_ldi_8
          && cmd.Op2.type == o_reg
          && cmd.Op2.reg == rR1 )
        {
            insn_t current_insn = cmd;
            next_insn(cmd.ea + cmd.size);
            if ( cmd.itype == fr_extsb
              && cmd.Op1.type == o_reg
              && cmd.Op1.reg == rR1 )
            {
              ok = true;
            }
            if ( ok )
            {
              ok = false;
              next_insn(cmd.ea + cmd.size);
              if ( cmd.itype == fr_addn
                && cmd.Op1.type == o_reg
                && cmd.Op1.reg == rR14
                && cmd.Op2.type == o_reg
                && cmd.Op2.reg == rR1 )
              {
                ok = true;
              }
            }
            cmd = current_insn;
        }
        // ldi32 #our_value, Ri
        // addn R14, Ri
        //
        // (where Ri is either R1 or R2)
        else if ( cmd.itype == fr_ldi_32
               && cmd.Op2.type == o_reg
               && (cmd.Op2.reg == rR1 || cmd.Op2.reg == rR2) )
        {
          ushort the_reg = cmd.Op2.reg;
          insn_t current_insn = cmd;
          next_insn(cmd.ea + cmd.size);
          if ( cmd.itype == fr_addn
            && cmd.Op1.type == o_reg
            && cmd.Op1.reg == rR14
            && cmd.Op2.type == o_reg
            && cmd.Op2.reg == the_reg )
          {
            ok = true;
          }
          cmd = current_insn;
        }

        if ( ok && may_create_stkvars() && !isDefArg(uFlag, op.n) )
        {
          func_t *pfn = get_func(cmd.ea);
          if ( pfn != NULL && pfn->flags & FUNC_FRAME )
          {
            if ( ua_stkvar2(op, op.value, 0) )
              op_stkvar(cmd.ea, op.n);
          }
        }
      }
      break;

    case o_displ:
      if( may_create_stkvars()  && !isDefArg(uFlag, op.n))
      {
        //if( op_displ_imm_r15( op ) )
        //{
        //    msg("0x%a: R15 v: 0x%a \n", cmd.ea, op.value);
        //}
        switch(cmd.itype)
        {
        case fr_ld:
        case fr_lduh:
        case fr_ldub:
        case fr_st:
        case fr_sth:
        case fr_stb:
          fr_create_lvar(op, op.value);
          break;
        }
      }
      break;

    case o_phrase:  // XXX
      if ((cmd.itype == fr_call || cmd.itype == fr_jmp)
        && cmd.Op1.type == o_phrase
        && cmd.Op1.specflag2 == fIGR)
      {
        cref_t reftype = cmd.itype == fr_call ? fl_CN : fl_JN;
        const int callreg = cmd.Op1.reg;
        insn_t cmd_backup = cmd;
        ea_t to = 0;
        if( decode_prev_insn(cmd.ea ) != BADADDR
            && cmd.itype == fr_ldi_32
            && cmd.Op1.type == o_imm
            && cmd.Op2.type == o_reg
            && cmd.Op2.reg == callreg )
        {
          offset = true;
          to = toEA(cmd.cs, cmd.Op1.value);
        }
        cmd = cmd_backup;  
        if( offset ) 
        {
          // if ( !isDefArg(uFlag, 0) ) 
          //msg("0x%a handle_operand o_phrase ua_add_cref: to:0x%a\n", cmd.ea, to);
          ua_add_cref(0, to, reftype);
        }
      }
      break;

    case o_reglist:
    case o_void:
    case o_reg:
      break;

    default:
      INTERR(10017);
  }
}

inline bool is_stop (void)
{
  uint32 feature = cmd.get_canon_feature();
  return (feature & CF_STOP) != 0;
}

static bool add_stkpnt(sval_t delta)
{
  func_t *pfn = get_func(cmd.ea);
  if ( pfn == NULL )
    return false;

  return add_auto_stkpnt2(pfn, cmd.ea+cmd.size, delta);
}

static void add_savedreg(int reg, ea_t ea, int offset)
{
  func_t *pfn = get_func(cmd.ea);
  if ( pfn == NULL )
    return;

  sval_t soff = get_spd(pfn, ea);
  sval_t res = pfn->frregs + (soff-offset);
  //msg("0x%a add_savedreg reg %d regnam %s, sval %x, frregs %x off %x res %x\n", ea, reg, savedRegNames[reg], pfn->frregs, soff, offset, res); 

  add_stkvar2(pfn, savedRegNames[reg], res, dwrdflag(), NULL, 4);
}

static void trace_sp(void)
{
  int i = 0;
  int j = 0;

  // trace SP changes
  switch ( cmd.itype )
  {
  case fr_stm0:
  case fr_stm1:
    j = 0;
    for(i =0; i < 8; i++ )
    {
      if( cmd.Op1.value & (1<<i))
      {
        j += 4;
        add_savedreg(((cmd.itype== fr_stm1)?rR15:rR7)-i, cmd.ea, j);
      }
    }
    add_stkpnt(-j);
    break;

  case fr_ldm0:
  case fr_ldm1:
    j = 0;
    for(i =0; i < 8; i++ )
    {
      if( cmd.Op1.value & (1<<i))
      {
        j += 4;
      }
    }
    add_stkpnt(j);
    break;

	case fr_st:
    if( cmd.Op2.type == o_phrase && cmd.Op2.reg == rR15)
    {
      if( cmd.Op2.specflag2 == fIGRP )
        add_stkpnt(4);

      if( cmd.Op2.specflag2 == fIGRM )
      {
        if( cmd.Op1.type == o_reg )
          add_savedreg(cmd.Op1.reg, cmd.ea, 4);
        add_stkpnt(-4);
      }
    }
    break;

  case fr_ld:
    if( cmd.Op2.type == o_phrase && cmd.Op2.reg == rR15)
    {
      if( cmd.Op2.specflag2 == fIGRP )
        add_stkpnt(4);

      if( cmd.Op2.specflag2 == fIGRM )
        add_stkpnt(-4);
    }
    break;

  case fr_enter:
    add_savedreg(rUSP, cmd.ea, 4);
    add_stkpnt(-((sval_t)cmd.Op1.value));
    op_num(cmd.ea, 0);
    break;

  case fr_leave:
    // not sure how to handle
    break;

  case fr_addsp:
    add_stkpnt(cmd.Op1.value);
    break;
  }
}

void search_stack_vars()
{
  // mov r14, rX
  // add2 n, rX

  // ldi:8 n, rX
  // extsb rX
  // add r14, rX
  
  //msg("%a search_stack_vars\n" cmd.ea);

  func_t *pfn = get_func(cmd.ea);
  if ( pfn != NULL && pfn->flags & FUNC_FRAME )
  {
    if( cmd.itype == fr_add2 &&
        cmd.Op1.type == o_imm &&
        cmd.Op2.type == o_reg)
    {
      //msg("%a search_stack_vars add2\n", cmd.ea);
      // trace backwards for add2
      SearchBackwardsForStackAssign search;
      if( search.Search( cmd.ea, cmd.Op2.reg ))
      {
        if(may_create_stkvars())
        {
          ua_stkvar2(cmd.Op1, cmd.Op1.value, 0);
          op_stkvar(cmd.ea, cmd.Op1.n);
        }
      }
    }
    else if( cmd.itype == fr_add &&
      cmd.Op1.type == o_reg &&
      cmd.Op1.reg == rR14 && 
      cmd.Op2.type == o_reg )
    {
      // trace backwards for setting. ldi:8 & extsb || ldi:32
      //msg("0x%a search_stack_vars add r14, rX\n", cmd.ea);
      SearchBackwardsForExtend extend;
      SearchBackwardsForLdi8 ldi8;
      SearchBackwardsForLdi32 ldi32;
      ea_t save_ea = cmd.ea;

      if( extend.Search(cmd.ea, cmd.Op2.reg) )
      {
        if( ldi8.Search(extend.match_ea, cmd.Op2.reg))
        {
          decode_insn(ldi8.match_ea);
          ua_stkvar2(cmd.Op1, cmd.Op1.value, 0);
          op_stkvar(cmd.ea, cmd.Op1.n);

          decode_insn(save_ea);
        }
      }
      else if( ldi32.Search(cmd.ea, cmd.Op2.reg))
      {
          //msg("0x%a SearchBackwardsForLdi32\n", ldi32.match_ea);

          decode_insn(ldi32.match_ea);
          ua_stkvar2(cmd.Op1, cmd.Op1.value, 0);
          op_stkvar(cmd.ea, cmd.Op1.n);

          decode_insn(save_ea);
      }
    }
  }
}


// Emulate an instruction.
int idaapi emu(void)
{

  bool flow = (!is_stop()) || (cmd.auxpref & INSN_DELAY_SHOT);
  //msg("0x%a flow0 %d\n", cmd.ea, flow);
  if ( flow )
  {
    insn_t cmd_backup = cmd;
    if ( decode_prev_insn(cmd.ea) != BADADDR )
    {
      flow = !(is_stop() && (cmd.auxpref & INSN_DELAY_SHOT));
      //msg("0x%a flow1 %d\n", cmd.ea, flow);

      if(!flow)
      {
        xrefblk_t xb;
        if( xb.first_to(cmd_backup.ea, XREF_ALL) )
          flow = xb.next_to();

        //msg("0x%a flow2 %d\n", cmd.ea, flow);
      }
    }
    cmd = cmd_backup;
  }

  if ( cmd.Op1.type != o_void ) handle_operand(cmd.Op1);
  if ( cmd.Op2.type != o_void ) handle_operand(cmd.Op2);
  if ( cmd.Op3.type != o_void ) handle_operand(cmd.Op3);
  if ( cmd.Op4.type != o_void ) handle_operand(cmd.Op4);

  if ( flow )
    ua_add_cref(0, cmd.ea + cmd.size, fl_F);

  if ( may_trace_sp() )
  {
    if ( !flow )
      recalc_spd(cmd.ea);     // recalculate SP register for the next insn
    else
      trace_sp();

    search_stack_vars();
  }
  return 1;
}

// Create a function frame
bool idaapi create_func_frame(func_t *pfn)
{
  ushort savedreg_size = 0;
  uint32 args_size = 0;
  uint32 localvar_size;

  ea_t ea = pfn->startEA;
  bool loopAgain = true;

  while( decode_insn(ea) != 0 && loopAgain )
  {
    loopAgain = false;
    if( cmd.itype == fr_stm0 || cmd.itype == fr_stm1)
    {
      for(int i =0; i < 8; i++ )
      {
        if( cmd.Op1.value & (1<<i))
        {
          savedreg_size += 4;
        }
      }
      //msg("0x%a create_func_frame: detected stmX 0x%a\n", ea, cmd.Op1.value);
      loopAgain = true;
    }

    // detect multiple ``st Ri, @-R15'' instructions.
    if (cmd.itype == fr_st
      && cmd.Op1.type == o_reg
      && cmd.Op2.type == o_phrase
      && cmd.Op2.reg == rR15
      && cmd.Op2.specflag2 == fIGRM)
    {
      savedreg_size += 4;
      //msg("0x%a create_func_frame: detected st Rx, @-R15\n", ea);
      loopAgain = true;
    }
    if( loopAgain )
      ea = cmd.ea + cmd.size;
  }

  // detect enter #nn
  if ( cmd.itype == fr_enter )
  {
    // R14 is automatically pushed by fr_enter
    savedreg_size += 4;
    localvar_size = uint32(cmd.Op1.value - 4);
    pfn->flags |= FUNC_FRAME;
    //msg("0x%a create_func_frame: detected enter #0x%a\n", ea, cmd.Op1.value);
  }
  // detect mov R15, R14 + ldi #imm, R0 instructions
  else
  {
    if ( cmd.itype != fr_mov
      || cmd.Op1.type != o_reg
      || cmd.Op1.reg != rR15
      || cmd.Op2.type != o_reg
      || cmd.Op2.reg != rR14 )
    {
      goto BAD_FUNC;
    }

    /*ea = */next_insn(ea);
    if ( (cmd.itype == fr_ldi_20 || cmd.itype == fr_ldi_32)
      && cmd.Op1.type == o_imm
      && cmd.Op2.type == o_reg
      && cmd.Op2.reg == rR0 )
    {
      localvar_size = uint32(cmd.Op1.value);
    }
    else
    {
      goto BAD_FUNC;
    }
    //msg("0x%a create_func_frame: detected ldi #0x%a, R0\n", ea, cmd.Op1.value);
  }

  //msg("0x%a create_func_frame: add_frame lvar_size: %x sreg_size: %x arg_Size: %x\n", ea, localvar_size, savedreg_size, args_size);
  return add_frame(pfn, localvar_size, savedreg_size, args_size);

BAD_FUNC:
  //msg("0x%a create_func_frame: bad function or non-frame based\n", ea);
  return 0;
}

int idaapi get_frame_retsize(func_t *pfn)
{
  return 0;
}

int idaapi is_sp_based(const op_t &x)
{
  // R15 is SP
  // R14 is FP
  return OP_SP_ADD | (op_displ_imm_r15(x) ? OP_SP_BASED : OP_FP_BASED);
}

int idaapi is_align_insn(ea_t ea)
{
  return get_byte(ea) == 0;
}

//--------------------------------------------------------------------------
static const int rv_fr[]  = { rR4, rR5, rR6, rR7, -1 };

int get_fr_fastcall_regs(const int **regs)
{
  *regs = rv_fr;
  return qnumber(rv_fr) - 1;
}