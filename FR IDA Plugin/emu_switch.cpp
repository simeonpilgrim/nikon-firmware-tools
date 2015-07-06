
#include "fr.hpp"
#include "jptcmn.cpp" // "../jptcmn.cpp"


THREAD_SAFE AS_PRINTF(1, 2) int swi_msg(const char *format, ...)
{
#ifdef FR_SWITCH_DEBUG
  va_list va;
  va_start(va, format);
  int nbytes = vmsg(format, va);
  va_end(va);
  return nbytes;
#else
  return 0;
#endif // FR_SWITCH_DEBUG
}

// Normal with imm guard
//ROM:001E6DB6 6                cmp     #COUNT, rD
//ROM:001E6DB8 5                bnc:D   switch_end_OR_default
//ROM:001E6DBA 4                mov     rD, rC
//ROM:001E6DBC 3                ldi:32  #off_XXXX, rB
//ROM:001E6DC2 2                lsl     #2, rC
//ROM:001E6DC4 1                ld      @(rC, rB), rA
//ROM:001E6DC6 0                jmp     @rA

// with reg guard and min
//ROM:000B0486 8*               add2    #-MIN, rD
//ROM:000B0488 7*               ldi:8   #COUNT, rE
//ROM:000B048A 6*               cmp     rE, rD
//ROM:000B048C 5                bnc:D   switch_end_OR_default    
//ROM:000B048E 4                mov     rD, rC
//ROM:000B0490 3                ldi:32  #off_XXXX, rB
//ROM:000B0496 2                lsl     #2, rC
//ROM:000B0498 1                ld      @(rC, rB), rA
//ROM:000B049A 0                jmp     @rA

// simple with long default jump, and short skip
//ROM:000B24F6 6                cmp     #COUNT, rD
//ROM:000B24F8 5*                bc      loc_short
//ROM:000B24FA 5*                ldi:32  #loc_long_Default_or_End, r12
//ROM:000B2500 5*                jmp:D   @r12
//ROM:000B2502
//ROM:000B2502 loc_short:                              
//ROM:000B2502 4                mov     rD, rC
//ROM:000B2504 3                ldi:32  #off_XXXX, rB
//ROM:000B250A 2                lsl     #2, rC
//ROM:000B250C 1                ld      @(rC, rB), rA
//ROM:000B250E 0                jmp     @rA            

static const char roots_fr_jmp[] = { 1, 0 };
static const char depends_fr_jmp[][2] = { 
  { 1 },        // 0
  { 2, 3 },        // 1
  { 4 },        // 2
  { 0 },        // 3
  { -5, -8 },        // 4
  { 6 },        // 5
  { -7 },        // 6
  { -8 },        // 7
  { 0 },        // 8
};

class fr_jmp_pattern_t : public jump_pattern_t
{
protected:
  enum { er0 = 1, er1, er2, er3, er4 };

  fr_jmp_pattern_t(const char *_roots, const char (*_depends)[2], switch_info_ex_t &_si)
    : jump_pattern_t(_roots, _depends, _si)
  {
    allow_noflows = false;
  }

public:
  virtual void check_spoiled(void);
  fr_jmp_pattern_t(switch_info_ex_t &_si) : jump_pattern_t(roots_fr_jmp, depends_fr_jmp, _si)
  {
    allow_noflows = false;
  }

  virtual bool jpi8(void);
  virtual bool jpi7(void);
  virtual bool jpi6(void);
  virtual bool jpi5(void);
  virtual bool jpi4(void);
  virtual bool jpi3(void);
  virtual bool jpi2(void);
  virtual bool jpi1(void);
  virtual bool jpi0(void);
};

//--------------------------------------------------------------------------
static bool is_reg_spoiled(uint16 reg)
{
  static uint32 specreg[1];

  const uint32 *spoil_list;
  size_t spoil_sz;
    specreg[0] = reg;
    spoil_list = &specreg[0];
    spoil_sz = 1;

  int spoiled_idx = get_spoiled_reg(spoil_list, spoil_sz);

  swi_msg("0x%a is_reg_spoiled reg: %d: %d\n", cmd.ea, reg, spoiled_idx);

  return spoiled_idx >= 0;
}


void fr_jmp_pattern_t::check_spoiled(void)
{
  if ( r[er0] != -1 && is_reg_spoiled(r[er0]) )
    spoiled[er0] = true;
  if ( r[er1] != -1 && is_reg_spoiled(r[er1]) )
    spoiled[er1] = true;
  if ( r[er2] != -1 && is_reg_spoiled(r[er2]) )
    spoiled[er2] = true;
}


//--------------------------------------------------------------------------
bool fr_jmp_pattern_t::jpi8(void)
{
  swi_msg("0x%a jpi8 add2    #-MIN, rD\n", cmd.ea);
  //add2    #-MIN, rD

  swi_msg("type: %d Op1.t %d, Op1.value %d, Op2.t %d, Op2.reg %d\n", 
    cmd.itype, cmd.Op1.type, cmd.Op1.value, cmd.Op2.type, cmd.Op2.reg);


  if ( cmd.itype == fr_add2
    && cmd.Op1.type == o_imm
    && cmd.Op2.type == o_reg
    && cmd.Op2.reg == r[er3] )
  {
    si.startea = cmd.ea;
    si.lowcase = (uval_t)(-((int)cmd.Op1.value));
    return true;
  }

  swi_msg("0x%a false\n", cmd.ea);
  return false;
}


//--------------------------------------------------------------------------
bool fr_jmp_pattern_t::jpi7(void)
{
  swi_msg("0x%a jpi7 ldi:8   #COUNT, rE\n", cmd.ea);
  //ldi:8   #COUNT, rE

  //swi_msg("type: %d Op1.t %d, Op1.value %d, Op2.t %d, Op2.reg %d\n", 
  //  cmd.itype, cmd.Op1.type, cmd.Op1.value, cmd.Op2.type, cmd.Op2.reg);


  if ( cmd.itype == fr_ldi_8
    && cmd.Op1.type == o_imm
    && cmd.Op2.type == o_reg
    && cmd.Op2.reg == r[er4] )
  {
    si.startea = cmd.ea;
    si.set_expr(cmd.Op2.reg, cmd.Op2.dtyp);
    si.ncases = cmd.Op1.value;
    si.jcases = cmd.Op1.value;
    return true;
  }

  swi_msg("0x%a false\n", cmd.ea);
  return false;
}

//--------------------------------------------------------------------------
bool fr_jmp_pattern_t::jpi6(void)
{
  swi_msg("0x%a jpi6 cmp  #COUNT, rD\n", cmd.ea);
  //cmp     rE, rD
  //cmp     #COUNT, rD

  //swi_msg("type: %d Op1.t %d, Op1.value %d, Op2.t %d, Op2.reg %d\n", 
  //  cmd.itype, cmd.Op1.type, cmd.Op1.value, cmd.Op2.type, cmd.Op2.reg);


  if ( cmd.itype == fr_cmp
    && cmd.Op1.type == o_imm
    && cmd.Op2.type == o_reg
    && cmd.Op2.reg == r[er3] )
  {
    si.startea = cmd.ea;
    si.set_expr(cmd.Op2.reg, cmd.Op2.dtyp);
    si.ncases = cmd.Op1.value;
    si.jcases = cmd.Op1.value;
    skip[7] = true;
    return true;
  }

  if ( cmd.itype == fr_cmp
    && cmd.Op1.type == o_reg
    && cmd.Op2.type == o_reg
    && cmd.Op2.reg == r[er3] )
  {
    r[er4] = cmd.Op1.reg;
    si.set_expr(cmd.Op2.reg, cmd.Op2.dtyp);
    return true;
  }

  swi_msg("0x%a false\n", cmd.ea);
  return false;
}

//--------------------------------------------------------------------------
bool fr_jmp_pattern_t::jpi5(void)
{
  swi_msg("0x%a jpi5 bnc:D   locret_1E6DD6\n", cmd.ea);
  //bnc:D   locret_1E6DD6

  if ( cmd.itype == fr_bnc )
  {
    si.defjump = toEA(cmd.cs, cmd.Op1.addr);
    si.flags |= SWI_DEFAULT;
    return true;
  }

  swi_msg("0x%a false\n", cmd.ea);
  return false;
}

//--------------------------------------------------------------------------
bool fr_jmp_pattern_t::jpi4(void)
{
  swi_msg("0x%a jpi4 mov     r4, r13\n", cmd.ea);
  //mov     r4, r13

  swi_msg("type: %d Op1.t %d, Op1.reg %d, Op2.t %d, Op2.reg %d\n", 
    cmd.itype, cmd.Op1.type, cmd.Op1.reg, cmd.Op2.type, cmd.Op2.reg);

  if ( cmd.itype == fr_mov 
    && cmd.Op1.type == o_reg
    && cmd.Op2.type == o_reg
    && cmd.Op2.reg == r[er2])
  {
    r[er3] = cmd.Op1.reg;
    swi_msg("0x%a r[er3] = %d\n", cmd.ea, r[er2]);
    si.startea = cmd.ea;
    si.lowcase = 0;
    return true;
  }

  swi_msg("0x%a false\n", cmd.ea);
  return false;
}

//--------------------------------------------------------------------------
bool fr_jmp_pattern_t::jpi3(void)
{
  swi_msg("0x%a jpi3 ldi:32  #off_XXXXX, r12\n", cmd.ea);

  //ldi:32  #off_28BA40, r12

  if ( cmd.itype == fr_ldi_32 
    && cmd.Op2.type == o_reg
    && cmd.Op2.reg == r[er1])
  {
    si.jumps = toEA(cmd.cs, cmd.Op1.value);
    si.set_jtable_element_size(4);
    return true;
  }

  swi_msg("0x%a false\n", cmd.ea);
  return false;
}

//--------------------------------------------------------------------------
bool fr_jmp_pattern_t::jpi2(void)
{
  swi_msg("0x%a jpi2 lsl     #2, r13\n", cmd.ea);

  //lsl     #2, r13
  swi_msg("type: %d Op1.t %d, Op1.v %d, Op2.t %d, Op2.reg %d\n", 
    cmd.itype, cmd.Op1.type, cmd.Op1.value, cmd.Op2.type, cmd.Op2.reg);

  if ( cmd.itype == fr_lsl 
    && cmd.Op2.type == o_reg
    && cmd.Op2.reg == rR13
    && cmd.Op1.type == o_imm
    && cmd.Op1.value == 2 )
  {
    return true;
  }
  swi_msg("0x%a false\n", cmd.ea);
  return false;
}

//--------------------------------------------------------------------------
bool fr_jmp_pattern_t::jpi1(void)
{
  swi_msg("0x%a jpi1\n", cmd.ea);

  //ld      @(r13, r12), r12

  if ( cmd.itype == fr_ld 
    && cmd.Op1.type == o_phrase
    && cmd.Op1.specflag2 == fR13RI
    && cmd.Op2.is_reg(r[er0]) ) 
  {
    r[er1] = cmd.Op1.reg;
    r[er2] = rR13;
    swi_msg("0x%a r[er1] = %d\n", cmd.ea, r[er1]);
    return true;
  }

  // is ldi:32 XXX, er0, then stop, as it's a normal jump
  if ( cmd.itype == fr_ldi_32 
    && cmd.Op2.type == o_reg
    && cmd.Op2.reg == r[er0])
  {
    this->failed = true;
    swi_msg("0x%a found normal jump, so skip all\n", cmd.ea);
    return false;
  }
  swi_msg("0x%a false\n", cmd.ea);
  return false;
}

//--------------------------------------------------------------------------
bool fr_jmp_pattern_t::jpi0(void)
{
  swi_msg("0x%a jpi0\n", cmd.ea);

  //   jmp     @r12

  if ( cmd.itype == fr_jmp
    && cmd.Op1.type == o_phrase
    && cmd.Op1.specflag2 == fIGR )
  {
    r[er0] = cmd.Op1.reg;
    swi_msg("0x%a r[er0] = %d\n", cmd.ea, r[er0]);

    return true;
  }
  swi_msg("0x%a false\n", cmd.ea);
  return false;
}


//----------------------------------------------------------------------
static jump_table_type_t is_fr_pattern(switch_info_ex_t &si)
{
  swi_msg("0x%a is_fr_pattern\n", cmd.ea);
  fr_jmp_pattern_t jp(si);
  return jp.match(cmd.ea) ? JT_FLAT32 : JT_NONE;
}

//----------------------------------------------------------------------
static bool check_for_jump1(switch_info_ex_t &si)
{
  swi_msg("0x%a check_for_jump1\n", cmd.ea);

  static is_pattern_t * const fr_patterns[] =
  {
    is_fr_pattern,
  };
  return check_for_table_jump2(fr_patterns, qnumber(fr_patterns), NULL, si);
}

//----------------------------------------------------------------------
bool idaapi fr_is_switch(switch_info_ex_t *si)
{
  if ( cmd.itype != fr_jmp )
    return false;

  swi_msg("0x%a fr_is_switch\n", cmd.ea);
  insn_t saved = cmd;
  bool found = check_for_jump1(*si);
  cmd = saved;
  return found;
}
