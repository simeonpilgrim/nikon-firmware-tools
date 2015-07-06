#pragma once

class SearchBackwards
{
private:
  virtual bool MatchFunc() { return false; };
public:
  bool Search(ea_t ea, uint16 reg);
  ea_t match_ea;
};

// used to search for 'mov r14, rX' after finding a 'add2 n, rX'
class SearchBackwardsForStackAssign : public SearchBackwards
{
  virtual bool MatchFunc() {
    return cmd.itype == fr_mov &&
      cmd.Op1.type == o_reg &&
      cmd.Op1.reg == rR14;
  }
};

// used to search for 'extsb rX' after finding a 'add r14, rX'
class SearchBackwardsForExtend : public SearchBackwards
{
  virtual bool MatchFunc() {
    return cmd.itype == fr_extsb ||
      cmd.itype == fr_extsh;
  }
};

// used to search for 'ldi:8 n, rX' after finding a 'add r14, rX'
class SearchBackwardsForLdi8 : public SearchBackwards
{
  virtual bool MatchFunc() {
    return cmd.itype == fr_ldi_8;
  }
};

// used to search for 'ldi:32 n, rX' after finding a 'add r14, rX'
class SearchBackwardsForLdi32 : public SearchBackwards
{
  virtual bool MatchFunc() {
    return cmd.itype == fr_ldi_32;
  }
};