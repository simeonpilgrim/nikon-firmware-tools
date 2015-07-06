
#include <enum.hpp>
#include "fr.hpp"
#include "emu_search.h"

static bool idaapi check_reg_for_stack_offset(ea_t ea, int reg);


THREAD_SAFE AS_PRINTF(1, 2) int type_msg(const char *format, ...)
{
#ifdef FR_TYPE_DEBUG
  va_list va;
  va_start(va, format);
  int nbytes = vmsg(format, va);
  va_end(va);
  return nbytes;
#else
  return 0;
#endif // FR_TYPE_DEBUG
}

bool fr_create_lvar(const op_t &x, uval_t v)
{
  //type_msg("0x%a create_lvar op.n %d v: 0x%a op.dtyp: %d\n", cmd.ea, x.n, v, (int)x.dtyp);
  func_t *pfn = get_func(cmd.ea);
  if ( pfn == NULL )
  {
    //type_msg("0x%a create_lvar: get_func failed\n", cmd.ea);
    return false;
  }

  sval_t delta;
  member_t *mptr = get_stkvar(x, v, &delta);
  if ( mptr == NULL )
  {
    if ( !ua_stkvar2(x, v, STKVAR_VALID_SIZE) )
    {
      //type_msg("0x%a create_lvar: ua_stkvar2 failed\n", cmd.ea);
      return false;
    }
    else
    {
      //type_msg("0x%a create_lvar: ua_stkvar2 0x%a v: 0x%a x.n: 0x%a\n", cmd.ea, x.value, v, x.n);
    }

    mptr = get_stkvar(x, v, &delta);
    if ( mptr == NULL ) 
    {
      //type_msg("0x%a create_lvar: 2nd get_stkvar failed\n", cmd.ea);
      return false;   // should not happen but better check
    }
  }

  //type_msg("0x%a create_lvar: delta: %x frregs: %x\n",cmd.ea, delta, pfn->frregs);
  
  if( delta > pfn->frregs )
  {
    //type_msg("0x%a create_lvar: regargqty %x\n",cmd.ea, pfn->regargqty);
    while ( pfn->regargqty < 4 )
    {
      //type_msg("0x%a create_lvar:    regargqty %x\n",cmd.ea, pfn->regargqty);
      add_regarg2(pfn, rR4 + pfn->regargqty, tinfo_t(BT_INT), NULL);
    }
  }

  bool ret = op_stkvar(cmd.ea, x.n);
  if( ret == 0 )
  {
    //type_msg("0x%a create_lvar: op_stkvar failed\n", cmd.ea);
  }
  return ret;
}


bool idaapi can_have_type(op_t &op)
{
  switch ( op.type )
  {
  case o_reg:
  case o_imm:
  case o_phrase:
  case o_displ:
    return true;
  }
  return false;
}

bool calc_fr_arglocs(func_type_data_t *fti)
{
  int r = 0;
  int n = fti->size();
  int spoff = 0;
  type_msg("0x%a calc_fr_arglocs n: %d\n", cmd.ea, n);

  for ( int i=0; i < n; i++ )
  {
    funcarg_t &fa = fti->at(i);
    size_t a = fa.type.get_size();
    type_msg("0x%a calc_fr_arglocs i: %d a: %d\n", cmd.ea, i, a);
    if ( a == BADSIZE )
      return false;

    if ( r < 4 )   // first 4 arguments are in r4, r5, r6, r7
    {
      if( a <= 4 )
        fa.argloc.set_reg1(rR4 + r);
      else if ( a <= 8 )
        fa.argloc.set_reg2(rR4 + r, rR5 + r);
      else // oh no super large data size, bail.
        return false;
    }
    else
    {
      spoff = align_up(spoff, 4);
      fa.argloc.set_stkoff(spoff);
      spoff += a;
    }

    a = align_up(a, inf.cc.size_i);
    r += int(a / 4);
  }
  
  fti->stkargs = spoff;
  return true;
}

//-------------------------------------------------------------------------
// returns:
//      -1: doesn't spoil anything
//      -2: spoils everything
//     >=0: the number of the register which is spoiled
static int spoils(const uint32 *regs, int n)
{
  switch (cmd.itype)
  {
  case fr_call:
    for (int i = 0; i < n; i++)
    {
      switch (regs[i]) {
      case rR0:
      case rR1:
      case rR2:
      case rR3:
      case rR4:
      case rR5:
      case rR6:
      case rR7:
      case rR12:
      case rR13:
        return i;
      }
    }
  }
  return get_spoiled_reg(regs, n);
}

static bool fr_set_op_type(
  const op_t &x,
  const tinfo_t &tif,
  const char *name,
  eavec_t &visited)
{
  type_msg("fr_set_op_type x.type %d name: %s\n",x.type, name);
  tinfo_t type = tif;

  switch ( x.type )
  {
  case o_imm:
    if ( type.is_ptr()
      && x.value != 0 )
      //&& !isDefArg(get_flags_novalue(cmd.ea), x.n) )
    {
      //type_msg("fr_set_op_type ptr 0x%a\n",x.value);
      if(remove_tinfo_pointer(idati, &type, &name))
        apply_once_tinfo_and_name(x.value, type, name);
      return set_offset(cmd.ea, x.n, 0);
    }
    else if( type.is_enum() && x.value != 0 )
    {
        qstring tname;
        type.get_type_name(&tname);
        enum_t enumt = get_enum(tname.c_str());
        op_enum(cmd.ea, x.n, enumt, 0);
        
        //type_msg("fr_set_op_type enum %s %x\n", tname.c_str(), enumt);
    }
    break;
  case o_mem:
    {
      //ea_t dea = calc_mem(x.addr);
      //return apply_once_tinfo_and_name(dea, type, name);
    }
  case o_displ:
    type_msg("fr_set_op_type apply_tinfo_to_stkarg op.value 0x%x op.addr \n",x.value, x.addr);
    return apply_tinfo_to_stkarg(x, x.addr, type, name);
  case o_reg:
    {
      uint32 r = x.reg;
      func_t *pfn = get_func(cmd.ea);
      if ( pfn == NULL )
        return false;
      bool ok;
      bool farref;
      func_item_iterator_t fii;
      for ( ok=fii.set(pfn, cmd.ea);
        ok && (ok=fii.decode_preceding_insn(&visited, &farref)) != false; )
      {
        if ( farref )
          continue;
        switch ( cmd.itype )
        {
        //case HPPA_ldo:
        //  if ( cmd.Op2.reg != r )
        //    continue;
        //  remove_tinfo_pointer(idati, &type, &name);
          // no break
        case fr_mov:
        case fr_ldub:
        case fr_lduh:
        case fr_ldi_8:
        case fr_ldi_20:
        case fr_ldi_32:
          if ( cmd.Op2.reg != r )
            continue;
          return fr_set_op_type(cmd.Op1, type, name, visited);
        default:
          {
            int code = spoils(&r, 1);
            if ( code == -1 )
              continue;
          }
          break;
        }
        break;
      }
      if ( !ok && cmd.ea == pfn->startEA )
      { // reached the function start, this looks like a register argument
        type_msg("fr_set_op_type add_regarg2\n");
        add_regarg2(pfn, r, type, name);
        break;
      }
    }
    break;
  }
  return false;
}

//-------------------------------------------------------------------------
//lint -e{1764} could be declared const ref
static bool idaapi set_op_type(op_t &x, const tinfo_t &type, const char *name)
{
  eavec_t visited;
  return fr_set_op_type(x, type, name, visited);
}

int use_fr_regarg_type(ea_t ea, const funcargvec_t &rargs)
{
  type_msg("0x%a use_fr_regarg_type\n", ea);
  int idx = -1;
  if ( decode_insn(ea) )
  {
    //type_msg("0x%a use_fr_regarg_type decoded\n", ea);
    qvector<uint32> regs;
    int n = rargs.size();
    regs.resize(n);
    for ( int i=0; i < n; i++ )
      regs[i] = rargs[i].argloc.reg1();

    idx = spoils(regs.begin(), n);
    type_msg("use_fr_regarg_type n: %x idx: %x\n", n,  idx);
    if ( idx >= 0 )
    {
      tinfo_t type = rargs[idx].type;
      const char *name = rargs[idx].name.begin();
      type_msg("use_fr_regarg_type idx: %d type: %d name: %s\n", idx, type, name);

      switch ( cmd.itype )
      {
      case fr_add2: // possible stkarg
        if( type.is_ptr() &&
            check_reg_for_stack_offset(cmd.ea, rargs[idx].argloc.reg1()))
        {
          type_msg("stack offset\n");
          if(may_create_stkvars())
            ua_stkvar2(cmd.Op1, cmd.Op1.value, 0);

          if ( remove_tinfo_pointer(idati, &type, &name) )
          {
            opinfo_t mt;
            size_t size;
            flags_t flags;
            if ( get_idainfo_by_type3(type, &size, &flags, &mt, NULL) )
            {
              //type_msg("add_stkvar2 0x%a name: %s off: %x flags: %x size: %x\n", cmd.ea, name, cmd.Op1.value, flags, size  );
              
              if( add_stkvar2(get_func(cmd.ea), name, cmd.Op1.value, flags, &mt, size) == 0 )
              {
                type_msg("add_stkvar2 failed\n" );
                struc_t* frame = get_frame(cmd.ea);

                sval_t delta;
                member_t *mptr = get_stkvar(cmd.Op1, cmd.Op1.value, &delta);
                if( mptr != NULL )
                {
                  //type_msg(" get_stvar soff: %x\n", mptr->soff);

                  //type_msg("del_struc_members frame %x f: %x t: %x\n", frame, mptr->soff, mptr->soff + size );
                  int delcount = del_struc_members(frame, mptr->soff, mptr->soff + size);
                  //type_msg("del_struc_members delcount %x \n", delcount);

                  if( add_stkvar2(get_func(cmd.ea), name, cmd.Op1.value, flags, &mt, size) == 0 )
                  {
                    type_msg("add_stkvar2 failed again\n" );
                  }
                }
              }
            }
            //ua_stkvar2(cmd.Op1, cmd.Op1.value, STKVAR_VALID_SIZE);
          }
        }
        else
        {
          // not stack arg usage so behave like default.
          idx |= REG_SPOIL;
        }
        break;

  //    case HPPA_ldo:
  //      remove_tinfo_pointer(idati, &type, &name);
        // no break
      case fr_mov:
      case fr_ldub:
      case fr_lduh:
      case fr_ldi_8:
      case fr_ldi_20:
      case fr_ldi_32:
        set_op_type(cmd.Op1, type, name);
         break;
      default: // unknown instruction changed the register, stop tracing it
        idx |= REG_SPOIL;
        break;
      }
    }
  }
  return idx;
}

bool calc_fr_retloc(const tinfo_t &tif, cm_t /*cc*/, argloc_t *retloc)
{
  type_msg("0%a calc_fr_retloc\n", cmd.ea);
  if( tif.is_void() )
    return true;

  if( tif.is_uint64() ||
    tif.is_int64() ||
    tif.is_double() )
  {
    retloc->set_reg2(rR4,rR5);
  }
  else
  {
    retloc->set_reg1(rR4);
  }
  return true;
}

bool is_basic_block_end(void)
{
  type_msg("0%a is_basic_block_end\n", cmd.ea);
  if ( (cmd.auxpref & INSN_DELAY_SHOT) != 0 )
    return true;
  return !isFlow(get_flags_novalue(cmd.ea+cmd.size));
}

//-------------------------------------------------------------------------
//----------------------------------------------------------------------
// does the specified address have a delay slot?
bool idaapi fr_has_delay_slot(ea_t ea)
{
  insn_t saved = cmd;
  bool res = false;

  if ( decode_insn(ea) )
  {
    if((cmd.auxpref & INSN_DELAY_SHOT) != 0 )
    {
      res =  true;
    }
  }
  cmd = saved;
  type_msg("0x%a fr_has_delay_slot %d\n", ea, res);
  return res;
}

static bool idaapi is_stkarg_load(int *src, int *dst)
{
  //type_msg("0x%a is_stkarg_load itype %d %x\n", cmd.ea, cmd.itype, cmd.Op2.specflag1);
  if ( cmd.itype == fr_st && 
    cmd.Op2.specflag1 == OP_DISPL_IMM_R15 )
  {
    *src = 0;
    *dst = 1;
    //type_msg("is_stkarg_load true addr: %x value %x\n", cmd.Op2.addr, cmd.Op2.value);
    cmd.Op2.addr = cmd.Op2.value;
    return true;
  }
  return false;
}

//-------------------------------------------------------------------------
void use_fr_arg_types(
  ea_t ea,
  func_type_data_t *fti,
  funcargvec_t *rargs)
{
  type_msg("0x%a use_fr_arg_types\n", ea);

  gen_use_arg_tinfos(ea, fti, rargs,
    set_op_type,
    is_stkarg_load,
    fr_has_delay_slot);
}



//-------------------------------------------------------------------------
// check if prior use of reg is a load from SP or FP 
//  mov     r14, r4
//  add2    #var_A, r4
// ea is off add2 instruction, search back for spoilage of reg, or usage 
static bool idaapi check_reg_for_stack_offset(ea_t ea, int reg)
{
  //type_msg("0x%a check_reg_for_stack_offset %d\n", ea, reg);

  SearchBackwardsForStackAssign search;

  return search.Search(ea, reg);
}


bool SearchBackwards::Search(ea_t ea, uint16 reg)
{
  //type_msg("0x%a SearchBackwards::Search %d\n", ea, reg);
  match_ea = BADADDR;

  func_t *pfn = get_func(ea); 
  decode_insn(ea);

  bool ret = false;

  while( decode_prev_insn(cmd.ea ) != BADADDR )
  {
    qvector<uint32> regs;
    regs.resize(1);
    regs[0] = reg;

    int idx = spoils(regs.begin(), 1);
    //type_msg(" SearchBackwards::Search spoil idx: %d\n", idx);

    if ( idx >= 0 )
    {
      if( MatchFunc() )
      {
        //type_msg(" SearchBackwards::Search spoil Match %d on %a:\n", reg, cmd.ea);
        match_ea = cmd.ea;
        ret = true;
        break; // while
      }
      else // unknown instruction changed the register, stop tracing it
      {
        break; // while
      }
    }

    if( cmd.ea == pfn->startEA )
      break; // while
  }

  // restore correct cmd.*
  decode_insn(ea);
  return ret;
}

