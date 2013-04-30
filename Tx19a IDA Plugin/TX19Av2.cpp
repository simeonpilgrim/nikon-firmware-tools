/*
 *      Toshiba TX19A processor extension plugin module for the Interactive disassembler (IDA) v5.2
 *
 *      Version 1.01
 *
 *      Jollyrogerxp 2009/2011
 *
 */

#include <ida.hpp>
#include <idp.hpp>
#include <kernwin.hpp>
#include <bytes.hpp>
#include <name.hpp>
#include <offset.hpp>
#include <segment.hpp>
#include <ua.hpp>
#include <auto.hpp>
#include <queue.hpp>
#include <lines.hpp>
#include <loader.hpp>
#include <srarea.hpp>

static ea_t ea; // current address within the instruction

static int mips16_segRegIndex;

int regTable[8] = 
{
	16,
	17,
	2,
	3,
	4,
	5,
	6,
	7
};

int moveRegTable[32] = 
{
	0, 
	8,
	16,
	24,
	1,
	9,
	17,
	25,
	2,
	10,
	18,
	26,
	3,
	11,
	19,
	27,
	4,
	12,
	20,
	28,
	5,
	13,
	21,
	29,
	6,
	14,
	22,
	30,
	7,
	15,
	23,
	31,
};

enum TX19A_insn_type_t
{
	// NORMAL

	TX19A_jr = CUSTOM_CMD_ITYPE,
	TX19A_jrc,
	TX19A_jalr,
	TX19A_jalrc,

	TX19A_movfp,

	TX19A_zeb,
	TX19A_zeh,
	TX19A_seb,
	TX19A_seh,

	TX19A_sll,
	TX19A_di,
	TX19A_ei,

	// EXTEND

	TX19A_bfins, // RR
	TX19A_sync,

	TX19A_addiu8, // ADDIU8
	TX19A_addmiu,
	TX19A_andi,
	TX19A_ori,
	TX19A_xori,
	TX19A_lui,

	TX19A_btst,  // SPECIAL
	TX19A_bclr,
	TX19A_bset,
	TX19A_bins,
	TX19A_bal,
	TX19A_bext,
	TX19A_lwfp,
	TX19A_swfp,

	TX19A_bteqz,  // I8
	TX19A_btnez,
	TX19A_swrasp,
	TX19A_adjsp,
//	TX19A_SVRS,
	TX19A_mov32r,
	TX19A_adjfp, 
	TX19A_movr32,

	TX19A_save,
	TX19A_restore,

	TX19A_jal,
	TX19A_jalx,

	TX19A_lbu,
	TX19A_li,
	TX19A_b,
	TX19A_beqz,
	TX19A_bnez,

	TX19A_mfc0,
	TX19A_mtc0,
	TX19A_srl,
	TX19A_sra,

	TX19A_addiu,
	TX19A_lb,
	TX19A_lh,
	TX19A_lhu,

	TX19A_lw,
	TX19A_mult,
	TX19A_multu,
	TX19A_div,
	TX19A_divu,
	TX19A_dive,
	TX19A_diveu,

	TX19A_addu,
	TX19A_subu,
	TX19A_mtlo,
	TX19A_mthi,

	TX19A_cmpi,
	TX19A_nop,

	TX19A_sb,
	TX19A_sh,
	TX19A_sw,

	TX19A_cmp,
	TX19A_neg,
 	TX19A_and,
 	TX19A_or,
 	TX19A_xor,
 	TX19A_not,

	TX19A_slti,
	TX19A_sltiu,

	TX19A_break,

	TX19A_mfhi,
	TX19A_mflo,

	TX19A_lwsp,
	TX19A_swsp,

	TX19A_slt,
	TX19A_sltu,
	TX19A_sllv,
	TX19A_slrv,
	TX19A_srav,

	TX19A_sadd,
	TX19A_ssub,
	TX19A_madd,
	TX19A_maddu,

	TX19A_eret,

	TX19A_unk_n,
	TX19A_unk_e,

	TX19A_last, 
};


instruc_t Instructions[] = {

	// NORMAL

	{ "jr", CF_USE1|CF_JUMP|CF_STOP },
	{ "jrc", CF_USE1|CF_JUMP|CF_STOP },
	{ "jalr", CF_USE1|CF_CALL },
	{ "jalrc", CF_USE1|CF_CALL },

	{ "movfp", CF_USE1 },

	{ "zeb", 0 },
	{ "zeh", 0 },
	{ "seb", 0 },
	{ "seh", 0 },

	{ "sll", 0 },
	{ "di", 0 },
	{ "ei", 0 },

	// EXTEND

	{ "bfins", 0 }, // RR
 	{ "sync", 0 },

	{ "addiu8", 0 }, // ADDIU8
	{ "addmiu", 0 },
	{ "andi", 0 },
	{ "ori", 0 },
	{ "xori", 0 },
	{ "lui", 0 },

	{ "btst", 0 },  // SPECIAL
	{ "bclr", 0 },
 	{ "bset", 0 },
 	{ "bins", 0 },
 	{ "bal", CF_USE1|CF_CALL },
 	{ "bext", 0 },
 	{ "lwfp", 0 },
 	{ "swfp", 0 },

	{ "bteqz", CF_USE1 }, // I8
 	{ "btnez", CF_USE1 },
 	{ "swrasp", 0 },
 	{ "addiu", 0 },
// 	{ "SVRS", 0 },
 	{ "mov32r", CF_USE1 },
 	{ "addiu", 0 },
 	{ "movr32", CF_USE1 },

 	{ "save", 0 },
 	{ "restore", 0 },

 	{ "jal", CF_USE1|CF_CALL },
 	{ "jalx", CF_USE1|CF_CALL },

 	{ "lbu", 0 },
 	{ "li", 0 },
 	{ "b", CF_USE1|CF_JUMP|CF_STOP },
	{ "beqz", CF_USE1|CF_USE2 },
	{ "bnez", CF_USE1|CF_USE2 },

	{ "mfc0", 0 },
	{ "mtc0", 0 },
	{ "srl", 0 },
	{ "sra", 0 },

	{ "addiu", 0 },
	{ "lb", 0 },
	{ "lh", 0 },
	{ "lhu", 0 },
	{ "lw", 0 },
	{ "mult", 0 },
	{ "multu", 0 },
	{ "div", 0 },
	{ "divu", 0 },
	{ "dive", 0 },
	{ "diveu", 0 },

	{ "addu", 0 },
	{ "subu", 0 },
	{ "mtlo", 0 },
	{ "mthi", 0 },

	{ "cmpi", 0 },
	{ "nop", 0 },

	{ "sb", 0 },
	{ "sh", 0 },
	{ "sw", 0 },

	{ "cmp", 0 },
	{ "neg", 0 },
 	{ "and", 0 },
 	{ "or", 0 },
 	{ "xor", 0 },
 	{ "not", 0 },

	{ "slti", 0 },
	{ "sltiu", 0 },

	{ "break", 0 },

	{ "mfhi", 0 },
	{ "mflo", 0 },

	{ "lwsp", 0 },
	{ "swsp", 0 },

	{ "slt", 0 },
	{ "sltu", 0 },
	{ "sllv", 0 },
	{ "slrv", 0 },
	{ "srav", 0 },

	{ "sadd", 0 },
	{ "ssub", 0 },
	{ "madd", 0 },
	{ "maddu", 0 },

	{ "eret", 0 },

	{ "unk_n", 0 },
	{ "unk_e", 0 },

};

int getMips16SegReg( unsigned int a )
{
	segreg_t *area = getSRarea( a );

	if( !area )
	{
		return 0;
	}

	int ret = area->reg( mips16_segRegIndex );

	return ret;
}
	


sval_t  extend_sign( uval_t val, int bit )
{
	uval_t mask = (1 << (bit + 1)) - 1;

	sval_t lbr = val & mask;
	if ( val & (1L<<bit) ) lbr |= ~uval_t(mask);  // extend sign

	return lbr;
}

int addiusp( unsigned int op, int extend )
{

	cmd.itype = TX19A_addiu;

	unsigned int rx;
	
	rx = (op >> 8) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[rx];

	cmd.Op2.type = o_reg;
	cmd.Op2.reg = 29;

	cmd.Op3.type = o_imm;

	if( !extend )
	{
		uval_t imm;

		imm = (op & 0xFF) << 2;
		
		cmd.Op3.value = imm;
		cmd.Op3.dtyp = dt_word;

		return 2;
	}
	else
	{		
		sval_t imm;

		imm = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
		
		cmd.Op3.value = imm;
		cmd.Op3.dtyp = dt_word;

		return 4;
	}
}

int extend_addiu8( unsigned int op )
{

	switch( ((op >> 5) & 0x7) )
	{
	
	case 0:
		cmd.itype = TX19A_addiu8;
		break;

	case 2:
		cmd.itype = TX19A_addmiu;
		break;

	case 4:
		cmd.itype = TX19A_andi;
		break;

	case 5:
		cmd.itype = TX19A_ori;
		break;

	case 6:
		cmd.itype = TX19A_xori;
		break;
	
	case 7:
		cmd.itype = TX19A_lui;
		break;

	default:
		return 0;
		break;
	};

	switch( cmd.itype )
	{
	case TX19A_addiu8:
		{
			unsigned int rx;
			
			rx = (op >> 8) & 0x7;

			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[rx];
			cmd.Op2.type = o_imm;
			
			sval_t imm;

			imm = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
			
			cmd.Op2.value = imm;
			cmd.Op2.dtyp = dt_word;

			return 4;

		}
		break;

	case TX19A_addmiu:
		return 4;
		break;

	case TX19A_lui:
	case TX19A_andi:
	case TX19A_ori:
	case TX19A_xori:
		{
			unsigned int ry;
			
			ry = (op >> 8) & 0x7;

			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[ry];
			cmd.Op2.type = o_imm;
			
			uval_t imm;

			imm = (op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 );
			
			cmd.Op2.value = imm;
			cmd.Op2.dtyp = dt_word;

			return 4;

		}
		break;

	default:
		cmd.itype = TX19A_unk_n;
		return 4;
		break;
	};	

}

int addiu8( unsigned int op )
{

	cmd.itype = TX19A_addiu8;

	unsigned int rx;
	
	rx = (op >> 8) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[rx];
	cmd.Op2.type = o_imm;
	
	sval_t imm;

	imm = extend_sign((op & 0xFF), 7);
	
	cmd.Op2.value = imm;
	cmd.Op2.dtyp = dt_word;

	return 2;

}

int special( unsigned int op, int extend )
{

	switch( ((op >> 8) & 0x7) )
	{
	
	case 0:
		cmd.itype = TX19A_btst;
		break;

	case 1:
		cmd.itype = TX19A_bclr;
		break;

	case 2:
		cmd.itype = TX19A_bset;
		break;

	case 3:
		cmd.itype = TX19A_bins;
		break;

	case 4:
		cmd.itype = TX19A_bal;
		break;

	case 5:
		cmd.itype = TX19A_bext;
		break;

	case 6:
		cmd.itype = TX19A_lwfp;
		break;
	
	case 7:
		cmd.itype = TX19A_swfp;
		break;

	default:
		return 0;
		break;
	};

	switch( cmd.itype )
	{
	case TX19A_btst:
	case TX19A_bclr:
	case TX19A_bset:
	case TX19A_bins:
	case TX19A_bext:
		{
			unsigned int pos3;
			unsigned int base;
			
			pos3 = (op >> 5) & 0x7;
			base = (op >> 19) & 0x3;

			if( extend != 0 && base == 0 )
			{
				sval_t offset;

				if( extend == 0 )
				{
					offset = extend_sign((op & 0x1F), 7);
				}
				else
				{
					offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x3) << 11 ), 15);
				}
	
				cmd.Op1.addr = offset;

			}
			else
			{
				uval_t offset;

				if( extend == 0 )
				{
					offset = (op & 0x1F);
				}
				else
				{
					offset = (op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x3) << 11 );
				}
	
				cmd.Op1.addr = offset;

			}

			cmd.Op1.type = o_displ;

			if( extend == 0 )
			{
				cmd.Op1.phrase = 30;
			}
			else
			{
				if( base != 0 )
				{
					cmd.Op1.phrase = base + 27;
				}
				else
				{
					cmd.Op1.phrase = 0;
				}
			}

			cmd.Op2.type = o_imm;
			cmd.Op2.value = pos3;
			cmd.Op2.dtyp = dt_byte;
			
			return 2 + (extend * 2);
		}
		break;

	case TX19A_bal:
		{
			sval_t offset;

			if( extend == 0 )
			{
				offset = extend_sign((op & 0xFF), 7) << 1;
			}
			else
			{
				offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15) << 1;
			}

			cmd.Op1.type  = o_near;
			cmd.Op1.addr  = cmd.ea + 2 + (extend * 2) + offset;
			cmd.Op1.dtyp  = dt_code;

			return 2 + (extend * 2);
		}
		break;


	case TX19A_lwfp:
	case TX19A_swfp:
		{
			unsigned int ry;
			short offset;
			
			ry = (op >> 5) & 0x7;
			offset = (op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 );

			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[ry];

			cmd.Op2.type = o_displ;
			cmd.Op2.addr = offset;
			cmd.Op2.phrase = 30;

			return 2 + (extend * 2);
		}
		break;

	default:
		if( extend )
			cmd.itype = TX19A_unk_e;
		else
			cmd.itype = TX19A_unk_n;
		return 2 + (extend * 2);
		break;
	}

}

int extend_rr( unsigned int op )
{
	
	switch( op & 0x1F )
	{

	case 0x7:
		if( (op & 0x4000000000) )
		{
			return 0;
		}
		else
		{
			unsigned int ry,rx, bit1, bit2;
			
			ry = (op >> 8) & 0x7;
			rx = (op >> 5) & 0x7;
			bit1 = (op >> 16) & 0x1F;
			bit2 = (op >> 21) & 0x1F;

			cmd.itype = TX19A_bfins;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[ry];
			cmd.Op2.type = o_reg;
			cmd.Op2.reg = regTable[rx];
			cmd.Op3.type = o_imm;
			cmd.Op3.value = bit2;
			cmd.Op3.dtyp = dt_byte;
			cmd.Op4.type = o_imm;
			cmd.Op4.value = bit1;
			cmd.Op4.dtyp = dt_byte;
			
			return 4;
		}
		break;

	case 0xF:

		cmd.itype = TX19A_sync;
		
		return 4;

		break;

	case 0x18:

		cmd.itype = TX19A_eret;

		return 4;

	default:
		cmd.itype = TX19A_unk_e;
		return 4;
		break;
	};
	
}

int movfp( unsigned int op )
{
	unsigned int rx;

	rx = (op >> 5) & 0x7;

	cmd.itype = TX19A_movfp;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = 30;
	cmd.Op2.type = o_reg;
	cmd.Op2.reg = rx;

	return 2;
}

int cnvt( unsigned int op )
{

	switch( (op >> 5) & 0x7 )
	{
	case 0:
		{
			cmd.itype = TX19A_zeb;
		}
		break;

	case 1:
		{
			cmd.itype = TX19A_zeh;
		}
		break;

	case 4:
		{
			cmd.itype = TX19A_seb;
		}
		break;

	case 5:
		{
			cmd.itype = TX19A_seh;
		}
		break;

	default:
		cmd.itype = TX19A_unk_n;
		return 2;
		break;
	};

	unsigned int rx;

	rx = (op >> 8) & 0x7;
	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[rx];

	return 2;

}

int jalrc( unsigned int op )
{
	switch( (op >> 5) & 0x7 )
	{
	case 0:
		{
			unsigned char rx = (op >> 8) & 0x7;
			cmd.itype = TX19A_jr;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[rx];
			cmd.Op1.dtyp  = dt_code;
			return 2;
		}
		break;

	case 1:
		{
			cmd.itype = TX19A_jr;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = 31;
			cmd.Op1.dtyp  = dt_code;
			return 2;
		}
		break;

	case 2:
		{
			unsigned char rx = (op >> 8) & 0x7;
			cmd.itype = TX19A_jalr;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = 31;
			cmd.Op2.type = o_reg;
			cmd.Op2.reg = regTable[rx];
			cmd.Op2.dtyp  = dt_code;
			return 2;
		}
		break;

	case 4:
		{
			unsigned char rx = (op >> 8) & 0x7;
			cmd.itype = TX19A_jrc;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[rx];
			cmd.Op1.dtyp  = dt_code;
			return 2;
		}
		break;

	case 5:
		{
			cmd.itype = TX19A_jrc;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = 31;
			cmd.Op1.dtyp  = dt_code;
			return 2;
		}
		break;

	case 6:
		{
			unsigned char rx = (op >> 8) & 0x7;
			cmd.itype = TX19A_jalrc;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = 31;
			cmd.Op2.type = o_reg;
			cmd.Op2.reg = regTable[rx];
			cmd.Op2.dtyp  = dt_code;
			return 2;
		}
		break;

	default:
		return 0;
		break;
	}


}

int mult3( unsigned int op )
{
	unsigned char ry = (op >> 5) & 0x7;
	unsigned char rx = (op >> 8) & 0x7;
	
	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];
	cmd.Op2.type = o_reg;
	cmd.Op2.reg = regTable[rx];
	cmd.Op3.type = o_reg;
	cmd.Op3.reg = regTable[ry];
	return 2;
}

int mult2( unsigned int op )
{
	unsigned char ry = (op >> 5) & 0x7;
	unsigned char rx = (op >> 8) & 0x7;
	
	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[rx];
	cmd.Op2.type = o_reg;
	cmd.Op2.reg = regTable[ry];
	return 2;
}

int break_( unsigned int op )
{
	cmd.itype = TX19A_break;
	cmd.Op1.type = o_imm;
	cmd.Op1.value = (op >> 5) & 0x3F;
	cmd.Op1.dtyp  = dt_word;
	return 2;
}

int rr( unsigned int op )
{

	switch( op & 0x1F )
	{

	case 0:
		return jalrc(op);
		break;

	case 2:
		cmd.itype = TX19A_slt;
		return mult2(op);
		break;

	case 3:
		cmd.itype = TX19A_sltu;
		return mult2(op);
		break;

	case 4:
		cmd.itype = TX19A_sllv;
		return mult2(op);
		break;

	case 5:
		return break_(op);
		break;

	case 6:
		cmd.itype = TX19A_slrv;
		return mult2(op);
		break;
	
	case 7:
		cmd.itype = TX19A_srav;
		return mult2(op);
		break;

	case 8:
		return movfp(op);
		break;

	case 0xA:
		cmd.itype = TX19A_cmp;
		break;

	case 0xB:
		cmd.itype = TX19A_neg;
		break;

	case 0xC:
		cmd.itype = TX19A_and;
		break;

	case 0xD:
		cmd.itype = TX19A_or;
		break;

	case 0xE:
		cmd.itype = TX19A_xor;
		break;

	case 0xF:
		cmd.itype = TX19A_not;
		break;

	case 0x10:
		{
			cmd.itype = TX19A_mfhi;
			unsigned char rx = (op >> 8) & 0x7;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[rx];
			return 2;
		}
		break;

	case 0x11:
		return cnvt(op);
		break;

	case 0x12:
		{
			cmd.itype = TX19A_mflo;
			unsigned char rx = (op >> 8) & 0x7;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[rx];
			return 2;
		}
		break;

	case 0x14:
		cmd.itype = TX19A_sadd;
		return mult3(op);
		break;

	case 0x15:
		cmd.itype = TX19A_ssub;
		return mult3(op);
		break;

	case 0x16:
		cmd.itype = TX19A_madd;
		return mult2(op);
		break;

	case 0x17:
		cmd.itype = TX19A_maddu;
		return mult2(op);
		break;

	case 0x18:
		cmd.itype = TX19A_mult;
		return mult2(op);
		break;

	case 0x19:
		cmd.itype = TX19A_multu;
		return mult2(op);
		break;

	case 0x1A:
		cmd.itype = TX19A_div;
		return mult2(op);
		break;

	case 0x1B:
		cmd.itype = TX19A_divu;
		return mult2(op);
		break;

	case 0x1C:
		cmd.itype = TX19A_mult;
		return mult3(op);
		break;

	case 0x1D:
		cmd.itype = TX19A_multu;
		return mult3(op);
		break;

	case 0x1E:
		cmd.itype = TX19A_dive;
		return mult2(op);
		break;

	case 0x1F:
		cmd.itype = TX19A_diveu;
		return mult2(op);
		break;

	default:
		cmd.itype = TX19A_unk_n;
		return 2;
		break;
	}

	switch( cmd.itype )
	{
	case TX19A_cmp:
	case TX19A_neg:
	case TX19A_and:
	case TX19A_or:
	case TX19A_xor:
	case TX19A_not:
		{		
			unsigned char ry = (op >> 5) & 0x7;
			unsigned char rx = (op >> 8) & 0x7;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[rx];
			cmd.Op2.type = o_reg;
			cmd.Op2.reg = regTable[ry];

			return 2;
		}
		break;
	default:
		cmd.itype = TX19A_unk_n;
		return 2;
		break;
	};

}

int rrr( unsigned int op )
{
	int opc = (op & 0x3) | ( ( (op >> 7) & 1 ) << 2 );

	switch( opc )
	{
	case 1:
	case 5:
		{
			unsigned char rz = (op >> 2) & 0x7;
			unsigned char ry = (op >> 5) & 0x7;
			unsigned char rx = (op >> 8) & 0x7;
			cmd.itype = TX19A_addu;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[rz];
			cmd.Op2.type = o_reg;
			cmd.Op2.reg = regTable[rx];
			cmd.Op3.type = o_reg;
			cmd.Op3.reg = regTable[ry];
			return 2;
		}
		break;

	case 2:
		{
			if( ((op >> 2) & 0x1F) != 0 )
			{
				unsigned int ry, sa5;

				cmd.itype = TX19A_sra;

				ry = (op >> 8) & 0x7;
				sa5 = (op >> 2) & 0x1F;

				cmd.Op1.type = o_reg;
				cmd.Op1.reg = regTable[ry];
				cmd.Op2.type = o_imm;
				cmd.Op2.value = sa5;
				cmd.Op2.dtyp = dt_byte;
			}
			else
			{
				unsigned int rx;

				cmd.itype = TX19A_mthi;

				rx = (op >> 8) & 0x7;

				cmd.Op1.type = o_reg;
				cmd.Op1.reg = regTable[rx];
			}
			return 2;
		}
		break;

	case 6:
		{
			if( ((op >> 2) & 0x1F) != 0 )
			{
				unsigned int ry, sa5;

				cmd.itype = TX19A_srl;

				ry = (op >> 8) & 0x7;
				sa5 = (op >> 2) & 0x1F;

				cmd.Op1.type = o_reg;
				cmd.Op1.reg = regTable[ry];
				cmd.Op2.type = o_imm;
				cmd.Op2.value = sa5;
				cmd.Op2.dtyp = dt_byte;
			}
			else
			{
				unsigned int rx;

				cmd.itype = TX19A_mtlo;

				rx = (op >> 8) & 0x7;

				cmd.Op1.type = o_reg;
				cmd.Op1.reg = regTable[rx];
			}
			return 2;
		}
		break;

	case 3:
	case 7:
		{
			unsigned char rz = (op >> 2) & 0x7;
			unsigned char ry = (op >> 5) & 0x7;
			unsigned char rx = (op >> 8) & 0x7;
			cmd.itype = TX19A_subu;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[rz];
			cmd.Op2.type = o_reg;
			cmd.Op2.reg = regTable[rx];
			cmd.Op3.type = o_reg;
			cmd.Op3.reg = regTable[ry];
			return 2;
		}
		break;


	case 4:
		if( ((op >> 2) & 0x1F) != 0 )
		{
			unsigned int ry, sa5;

			cmd.itype = TX19A_sll;
			ry = (op >> 8) & 0x7;
			sa5 = (op >> 2) & 0x1F;

			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[ry];
			cmd.Op2.type = o_imm;
			cmd.Op2.value = sa5;
			cmd.Op2.dtyp = dt_byte;
			return 2;
		}
		else
		{
			if( ((op >> 8) & 0x3) == 0 )
			{
				cmd.itype = TX19A_di;
				return 2;
			}
			else
			if( ((op >> 8) & 0x3) == 1 )
			{
				cmd.itype = TX19A_ei;
				return 2;
			}
			else
			{
				return 0;
			}
		}
		break;

	default:
		cmd.itype = TX19A_unk_n;
		return 2;
		break;
	};
}

int i8( unsigned int op, int extend )
{

	switch( ((op >> 8) & 0x7) )
	{
	
	case 0:
		cmd.itype = TX19A_bteqz;
		break;

	case 1:
		cmd.itype = TX19A_btnez;
		break;

	case 2:
		cmd.itype = TX19A_swrasp;
		break;

	case 3:
		cmd.itype = TX19A_adjsp;
		break;

	case 4:
		if( ((op >> 7) & 0x1) )
		{
			cmd.itype = TX19A_save;
		}
		else
		{
			cmd.itype = TX19A_restore;
		}
		break;

	case 5:
		cmd.itype = TX19A_mov32r;
		break;

	case 6:
		cmd.itype = TX19A_adjfp;
		break;
	
	case 7:
		cmd.itype = TX19A_movr32;
		break;

	default:
		return 0;
		break;
	};

	switch( cmd.itype )
	{
	case TX19A_bteqz:
	case TX19A_btnez:
		{
			sval_t offset;

			if( extend == 0 )
			{
				offset = extend_sign((op & 0xFF), 7) << 1;
			}
			else
			{
				offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15) << 1;
			}

			cmd.Op1.type  = o_near;
			cmd.Op1.addr  = cmd.ea + 2 + (extend * 2) + offset;
			cmd.Op1.dtyp  = dt_code;

			return 2 + (extend * 2);
		}
		break;

	case TX19A_save:
	case TX19A_restore:

		unsigned short framesize;

		if( extend == 0 )
		{
			framesize = (op & 0xF) << 3;
		}
		else
		{
			framesize = ((op & 0xF) | ( ((op >> 20) & 0xF) << 4 )) << 3;
		}

		cmd.Op1.type = o_imm;
		cmd.Op1.value = framesize;
		cmd.Op1.dtyp = dt_byte;

		return 2 + (extend * 2);

		break;

	case TX19A_adjsp:
		{
			sval_t offset;

			if( extend == 0 )
			{
				offset = extend_sign((op & 0xFF), 7) << 3;
				cmd.Op2.dtyp = dt_byte;
			}
			else
			{
				offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
				cmd.Op2.dtyp = dt_word;
			}

			cmd.Op1.type = o_reg;
			cmd.Op1.reg = 29;
			
			cmd.Op2.type = o_imm;
			cmd.Op2.value = offset;
	
			return 2 + (extend * 2);

		}
		break;

	case TX19A_adjfp:
		{
			sval_t offset;

			if( extend == 0 )
			{
				offset = extend_sign((op & 0xFF), 7) << 2;
				cmd.Op2.dtyp = dt_byte;
			}
			else
			{
				offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
				cmd.Op2.dtyp = dt_word;
			}

			cmd.Op1.type = o_reg;
			cmd.Op1.reg = 30;
			
			cmd.Op2.type = o_imm;
			cmd.Op2.value = offset;
	
			return 2 + (extend * 2);

		}
		break;

	case TX19A_mov32r:
		{
			unsigned int rz, r32;

			rz = (op & 0x7);
			r32 = ((op >> 3) & 0x1F);

			if( r32 != 0 )
			{
				cmd.Op1.type = o_reg;
				cmd.Op1.reg = moveRegTable[r32];

				cmd.Op2.type = o_reg;
				cmd.Op2.reg = regTable[rz];
			}
			else
			{
				cmd.itype = TX19A_nop;
			}

			return 2;
		}
		break;

	case TX19A_movr32:
		{
			unsigned int ry, r32;

			r32 = (op & 0x1F);
			ry = ((op >> 5) & 0x7);

			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[ry];
			
			cmd.Op2.type = o_reg;
			cmd.Op2.reg = r32;

			return 2;
		}
		break;

	default:
		if( extend )
			cmd.itype = TX19A_unk_e;
		else
			cmd.itype = TX19A_unk_n;
		return 2 + (extend * 2);
		break;

	};
}

int jal( unsigned int op )
{

	unsigned int offset;

	if( ( (op >> 26) & 1 ) == 1 )
	{
		cmd.itype = TX19A_jalx;
	}
	else
	{
		cmd.itype = TX19A_jal;
	}

	offset = ( (op & 0xFFFF) | ( ((op >> 21) & 0x1F) << 16 ) | ( ((op >> 16) & 0x1F) << 21 ) ) << 2;
	offset = ( (cmd.ea + 4) & 0xF0000000 ) | offset;

	cmd.Op1.type  = o_near;
	cmd.Op1.addr  = offset;
	cmd.Op1.dtyp  = dt_code;
	

	return 4;
}

int lbu( unsigned int op, int extend )
{

	unsigned int ry;
	
	cmd.itype = TX19A_lbu;

	ry = (op >> 5) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];
	cmd.Op2.type = o_imm;

	unsigned int base;
	
	base = (op >> 8) & 0x7;

	if( extend == 0 )
	{
		uval_t offset;
		offset = (op & 0x1F);
		cmd.Op2.addr = offset;
	}
	else
	{
		sval_t offset;
		offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
		cmd.Op2.addr = offset;
	}

	cmd.Op2.phrase = regTable[base];
	cmd.Op2.type = o_displ;


	return 2 + (extend * 2);
}

int li( unsigned int op, int extend )
{

	unsigned int ry;
	
	cmd.itype = TX19A_li;

	ry = (op >> 8) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];
	cmd.Op2.type = o_imm;	

	if( extend == 0 )
	{
		uval_t imm;
		imm = (op & 0xFF);
		cmd.Op2.value = imm;
		cmd.Op2.dtyp = dt_byte;
	}
	else
	{
		uval_t imm;
		imm = (op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 );
		cmd.Op2.value = imm;
		cmd.Op2.dtyp = dt_word;
	}


	return 2 + (extend * 2);
}

int b( unsigned int op, int extend )
{

	sval_t offset;

	cmd.itype = TX19A_b;

	if( extend == 0 )
	{
		offset = extend_sign((op & 0x7FF), 10) << 1;
	}
	else
	{
		offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15) << 1;
	}

	cmd.Op1.type  = o_near;
	cmd.Op1.addr  = cmd.ea + 2 + (extend * 2) + offset;
	cmd.Op1.dtyp  = dt_code;

	return 2 + (extend * 2);
}

int beqz( unsigned int op, int extend )
{

	sval_t offset;
	unsigned int ry;

	cmd.itype = TX19A_beqz;
	
	ry = (op >> 8) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];

	if( extend == 0 )
	{
		offset = extend_sign((op & 0xFF), 7) << 1;
	}
	else
	{
		offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15) << 1;
	}

	cmd.Op2.type  = o_near;
	cmd.Op2.addr  = cmd.ea + 2 + (extend * 2) + offset;
	cmd.Op2.dtyp  = dt_code;

	return 2 + (extend * 2);
}

int bnez( unsigned int op, int extend )
{

	sval_t offset;
	unsigned int ry;

	cmd.itype = TX19A_bnez;
	
	ry = (op >> 8) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];

	if( extend == 0 )
	{
		offset = extend_sign((op & 0xFF), 7) << 1;
	}
	else
	{
		offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15) << 1;
	}

	cmd.Op2.type  = o_near;
	cmd.Op2.addr  = cmd.ea + 2 + (extend * 2) + offset;
	cmd.Op2.dtyp  = dt_code;

	return 2 + (extend * 2);
}

int shift( unsigned int op, int extend )
{

	switch( op & 0x7 )
	{
	
	case 0:
	case 4:
		cmd.itype = TX19A_sll;
		break;

	case 1:
		cmd.itype = TX19A_mfc0;
		break;

	case 5:
		cmd.itype = TX19A_mtc0;
		break;

	case 2:
	case 6:
		cmd.itype = TX19A_srl;
		break;

	case 3:
	case 7:
		cmd.itype = TX19A_sra;
		break;

	default:
		return 0;
		break;
	};

	switch( cmd.itype )
	{

	case TX19A_sll:
	case TX19A_srl:
	case TX19A_sra:
		{
			unsigned int ry, rx, shift;
			
			rx = (op >> 8) & 0x7;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[rx];

			ry = (op >> 5) & 0x7;
			cmd.Op2.type = o_reg;
			cmd.Op2.reg = regTable[ry];
	
			if( extend == 0 )
			{
				shift = (op >> 2) & 0x7;
				if( shift == 0 ) shift = 8;
			}
			else
			{
				shift = (op >> 22) & 0x1F;
			}
		
			cmd.Op3.type = o_imm;	
			cmd.Op3.value = shift;
			cmd.Op3.dtyp = dt_byte;

			return 2 + (extend * 2);
		}
		break;

	case TX19A_mfc0:
	case TX19A_mtc0:
		{
			unsigned int ry, rx;
			
			ry = (op >> 8) & 0x7;
			cmd.Op1.type = o_reg;
			cmd.Op1.reg = regTable[ry];

			rx = (op >> 3) & 0x1F;
			cmd.Op2.type = o_imm;
			cmd.Op2.value = rx;
			cmd.Op2.dtyp = dt_byte;
	
			return 2;
		}
		break;

	default:
		if( extend )
			cmd.itype = TX19A_unk_e;
		else
			cmd.itype = TX19A_unk_n;
		return 2 + (extend * 2);
		break;

	};
}

int rria( unsigned int op, int extend )
{

	cmd.itype = TX19A_addiu;
	
	unsigned int ry, rx;

	ry = (op >> 5) & 0x7;
	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];

	rx = (op >> 8) & 0x7;
	cmd.Op2.type = o_reg;
	cmd.Op2.reg = regTable[rx];

	sval_t imm;

	if( extend == 0 )
	{
		imm = extend_sign((op & 0xF), 3);
	}
	else
	{
		imm = extend_sign((op & 0xF) | ( ((op >> 20) & 0x7F) << 4 ) | ( ((op >> 16) & 0xF) << 11 ), 14);
	}

	cmd.Op3.type = o_imm;	
	cmd.Op3.value = imm;
	cmd.Op3.dtyp = dt_word;


	return 2 + (extend * 2);
}

int lb_sb( unsigned int op, int extend )
{

	unsigned int ry;
	
	if( (op >> 14) & 1 )
	{
		cmd.itype = TX19A_sb;
	}
	else
	{
		cmd.itype = TX19A_lb;
	}

	ry = (op >> 5) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];
	cmd.Op2.type = o_imm;

	unsigned int base;
	
	base = (op >> 8) & 0x7;

	if( extend == 0 )
	{
		uval_t offset;
		offset = (op & 0x1F);
		cmd.Op2.addr = offset;
	}
	else
	{
		sval_t offset;
		offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
		cmd.Op2.addr = offset;
	}

	cmd.Op2.phrase = regTable[base];
	cmd.Op2.type = o_displ;


	return 2 + (extend * 2);
}

int lb_sb_sp( unsigned int op, int extend )
{

	unsigned int ry;
	
	if( (op >> 7) & 1 )
	{
		cmd.itype = TX19A_sb;
	}
	else
	{
		cmd.itype = TX19A_lb;
	}

	ry = (op >> 8) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];

	cmd.Op2.type = o_imm;

	if( extend == 0 )
	{
		uval_t offset;
		offset = (op & 0x7F);
		cmd.Op2.addr = offset;
	}
	else
	{
		sval_t offset;
		offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
		cmd.Op2.addr = offset;
	}

	cmd.Op2.phrase = 29;
	cmd.Op2.type = o_displ;


	return 2 + (extend * 2);
}

int lb_sb_fp( unsigned int op, int extend )
{

	unsigned int ry;
	
	if( (op >> 7) & 1 )
	{
		cmd.itype = TX19A_sb;
	}
	else
	{
		cmd.itype = TX19A_lb;
	}

	ry = (op >> 8) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];

	cmd.Op2.type = o_imm;

	if( extend == 0 )
	{
		uval_t offset;
		offset = (op & 0x7F);
		cmd.Op2.addr = offset;
	}
	else
	{
		sval_t offset;
		offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
		cmd.Op2.addr = offset;
	}

	cmd.Op2.phrase = 30;
	cmd.Op2.type = o_displ;


	return 2 + (extend * 2);
}

int lh_u( unsigned int op, int extend )
{
	unsigned int ry;	

	ry = (op >> 5) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];
	cmd.Op2.type = o_imm;

	unsigned int base;
	
	base = (op >> 8) & 0x7;

	if( extend == 0 )
	{
		uval_t offset;
		offset = (op & 0x1F) << 1;
		cmd.Op2.addr = offset;
	}
	else
	{
		sval_t offset;
		offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
		cmd.Op2.addr = offset;
	}

	cmd.Op2.phrase = regTable[base];
	cmd.Op2.type = o_displ;


	return 2 + (extend * 2);
}

int lw_sw( unsigned int op, int extend )
{
	unsigned int ry;	

	if( (op >> 14) & 1 )
	{
		cmd.itype = TX19A_sw;
	}
	else
	{
		cmd.itype = TX19A_lw;
	}

	ry = (op >> 5) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];
	
	cmd.Op2.type = o_imm;

	unsigned int base;
	
	base = (op >> 8) & 0x7;

	if( extend == 0 )
	{
		uval_t offset;
		offset = (op & 0x1F) << 2;
		cmd.Op2.addr = offset;
	}
	else
	{
		sval_t offset;
		offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
		cmd.Op2.addr = offset;
	}

	cmd.Op2.phrase = regTable[base];
	cmd.Op2.type = o_displ;


	return 2 + (extend * 2);
}

int lw_sw_sp( unsigned int op, int extend )
{
	unsigned int rx;	

	if( (op >> 14) & 1 )
	{
		cmd.itype = TX19A_swsp;
	}
	else
	{
		cmd.itype = TX19A_lwsp;
	}

	rx = (op >> 8) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[rx];
	
	cmd.Op2.type = o_imm;

	if( extend == 0 )
	{
		uval_t offset;
		offset = (op & 0xFF) << 2;
		cmd.Op2.addr = offset;
	}
	else
	{
		sval_t offset;
		offset = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
		cmd.Op2.addr = offset;
	}

	cmd.Op2.phrase = 29;
	cmd.Op2.type = o_displ;


	return 2 + (extend * 2);
}

int fp_sp_h( unsigned int op, int extend )
{
	unsigned int ry;	
	unsigned int subop = (op >> 7) & 1;

	if( subop )
	{
		cmd.itype = TX19A_sh;
	}
	else
	{
		cmd.itype = TX19A_lh;
	}

	ry = (op >> 8) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[ry];
	
	cmd.Op2.type = o_imm;

	if( extend == 0 )
	{
		uval_t offset;
		offset = (op & 0x7E);
		cmd.Op2.addr = offset;
	}
	else
	{
		sval_t offset;
		offset = extend_sign((op & 0x1E) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
		cmd.Op2.addr = offset;
	}

	if( op & 1 )
	{
		cmd.Op2.phrase = 30;
	}
	else
	{
		cmd.Op2.phrase = 29;
	}

	cmd.Op2.type = o_displ;


	return 2 + (extend * 2);
}

int cmpi( unsigned int op, int extend )
{

	unsigned int rx;
	
	cmd.itype = TX19A_cmpi;

	rx = (op >> 8) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[rx];
	cmd.Op2.type = o_imm;	

	if( extend == 0 )
	{
		uval_t imm;
		imm = (op & 0xFF);
		cmd.Op2.value = imm;
		cmd.Op2.dtyp = dt_byte;
	}
	else
	{
		uval_t imm;
		imm = (op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 );
		cmd.Op2.value = imm;
		cmd.Op2.dtyp = dt_word;
	}

	return 2 + (extend * 2);
}

int slti_u( unsigned int op, int extend )
{

	if( (op >> 14) & 1 )
	{
		cmd.itype = TX19A_sltiu;
	}
	else
	{
		cmd.itype = TX19A_slti;
	}

	unsigned int rx;
	
	rx = (op >> 8) & 0x7;

	cmd.Op1.type = o_reg;
	cmd.Op1.reg = regTable[rx];
	cmd.Op2.type = o_imm;	

	if( extend == 0 )
	{
		uval_t imm;
		imm = (op & 0xFF);
		cmd.Op2.value = imm;
		cmd.Op2.dtyp = dt_byte;
	}
	else
	{
		sval_t imm;
		imm = extend_sign((op & 0x1F) | ( ((op >> 21) & 0x3F) << 5 ) | ( ((op >> 16) & 0x1F) << 11 ), 15);
		cmd.Op2.value = imm;
		cmd.Op2.dtyp = dt_word;
	}

	return 2 + (extend * 2);
}

//--------------------------------------------------------------------------
// Analyze an instruction and fill the 'cmd' structure
int ana(void)
{
	if( getMips16SegReg( cmd.ea ) == 0 ) return 0;
	
	unsigned long opcode;
	
	unsigned long opcodelo;

	opcode = (unsigned long) (get_byte(cmd.ea+0)<<8) | (get_byte(cmd.ea+1));

	if( (opcode & 0xF800) == 0xF000 )
	{

		// Extend

		opcode = (unsigned long)(get_byte(cmd.ea+0)<<24)|(get_byte(cmd.ea+1)<<16)|(get_byte(cmd.ea+2)<<8)|get_byte(cmd.ea+3);
		
		opcodelo = (opcode >> 11) & 0x1f;

		switch( opcodelo )
		{

		case 0x0:
			// ADDIUSP
			return addiusp( opcode, 1 );
			break;
			
		case 0x2:
			// B
			return b( opcode, 1 );
			break;

		case 0x4:
			// BEQZ
			return beqz( opcode, 1 );
			break;

		case 0x5:
			// BNEZ
			return bnez( opcode, 1 );
			break;

		case 0x6:
			// SHIFT
			return shift( opcode, 1 );
			break;

		case 0x7:
			// LBFP
			return lb_sb_fp( opcode, 1 );
			break;

		case 0x8:
			// RRI-A
			return rria( opcode, 1 );
			break;

		case 0x9:
			// ADDIU8
			return extend_addiu8( opcode );
			break;

		case 0xA:
			// SLTI
			return slti_u( opcode, 1 );
			break;

		case 0xB:
			// SLTIU
			return slti_u( opcode, 1 );
			break;

		case 0xC:
			// I8
			return i8( opcode, 1 );
			break;

		case 0xD:
			// LI
			return li( opcode, 1 );
			break;

		case 0xE:
			// CMPI
			return cmpi( opcode, 1 );
			break;

		case 0xF:
			// LBSP
			return lb_sb_sp( opcode, 1 );
			break;

		case 0x10:
			// LB
			return lb_sb( opcode, 1 );
			break;

		case 0x11:
			// LH
			cmd.itype = TX19A_lh;
			return lh_u( opcode, 1 );
			break;

		case 0x12:
			// LWSP
			return lw_sw_sp( opcode, 1 );
			break;

		case 0x13:
			// LW
			return lw_sw( opcode, 1 );
			break;

		case 0x14:
			// LBU
			return lbu( opcode, 1 );
			break;

		case 0x15:
			// LHU
			cmd.itype = TX19A_lhu;
			return lh_u( opcode, 1 );
			break;

		case 0x17:
			// FP-SP-H
			return fp_sp_h( opcode, 1 );
			break;

		case 0x18:
			// SB
			return lb_sb( opcode, 1 );
			break;

		case 0x19:
			// SH
			cmd.itype = TX19A_sh;
			return lh_u( opcode, 1 );
			break;

		case 0x1A:
			// SWSP
			return lw_sw_sp( opcode, 1 );
			break;

		case 0x1B:
			// SW
			return lw_sw( opcode, 1 );
			break;

		case 0x1D:
			// RR
			return extend_rr( opcode );
			break;

		case 0x1F:
			// SPECIAL
			return special( opcode, 1 );
			break;

		default:
			cmd.itype = TX19A_unk_e;
			return 4;
			break;
		};
		
	}
	else
	{
		// Normal

		opcodelo = (opcode >> 11) & 0x1f;

		switch( opcodelo )
		{

		case 0x0:
			// ADDIUSP
			return addiusp( opcode, 0 );
			break;

		case 0x2:
			// B
			return b( opcode, 0 );
			break;

		case 0x3:
			// JAL(X)
			opcode = (unsigned long)(get_byte(cmd.ea+0)<<24)|(get_byte(cmd.ea+1)<<16)|(get_byte(cmd.ea+2)<<8)|get_byte(cmd.ea+3);
			return jal( opcode );
			break;

		case 0x4:
			// BEQZ
			return beqz( opcode, 0 );
			break;

		case 0x5:
			// BNEZ
			return bnez( opcode, 0 );
			break;

		case 0x6:
			// SHIFT
			return shift( opcode, 0 );
			break;

		case 0x7:
			// LBFP
			return lb_sb_fp( opcode, 0 );
			break;

		case 0x8:
			// RRI-A
			return rria( opcode, 0 );
			break;

		case 0x9:
			// ADDIU8
			return addiu8( opcode );
			break;

		case 0xA:
			// SLTI
			return slti_u( opcode, 0 );
			break;

		case 0xB:
			// SLTIU
			return slti_u( opcode, 0 );
			break;

		case 0xC:
			// I8
			return i8( opcode, 0 );
			break;

		case 0xD:
			// LI
			return li( opcode, 0 );
			break;

		case 0xE:
			// CMPI
			return cmpi( opcode, 0 );
			break;

		case 0xF:
			// LBSP
			return lb_sb_sp( opcode, 0 );
			break;

		case 0x10:
			// LB
			return lb_sb( opcode, 0 );
			break;

		case 0x11:
			// LH
			cmd.itype = TX19A_lh;
			return lh_u( opcode, 0 );
			break;

		case 0x12:
			// LWSP
			return lw_sw_sp( opcode, 0 );
			break;

		case 0x13:
			// LW
			return lw_sw( opcode, 0 );
			break;

		case 0x14:
			// LBU
			return lbu( opcode, 0 );
			break;

		case 0x15:
			// LHU
			cmd.itype = TX19A_lhu;
			return lh_u( opcode, 0 );
			break;

		case 0x17:
			// FP-SP-H
			return fp_sp_h( opcode, 0 );
			break;

		case 0x18:
			// SB
			return lb_sb( opcode, 0 );
			break;

		case 0x19:
			// SH
			cmd.itype = TX19A_sh;
			return lh_u( opcode, 0 );
			break;

		case 0x1A:
			// SWSP
			return lw_sw_sp( opcode, 0 );
			break;

		case 0x1B:
			// SW
			return lw_sw( opcode, 0 );
			break;

		case 0x1C:
			// RR
			return rrr( opcode );
			break;
			
		case 0x1D:
			// RR
			return rr( opcode );
			break;

		case 0x1F:
			// SPECIAL
			return special( opcode, 0 );
			break;

		default:
			cmd.itype = TX19A_unk_n;
			return 2;
			break;
		};
				
	}


	return 0;
}

//--------------------------------------------------------------------------
// Return the instruction mnemonics
const char *get_insn_mnem(void)
{
	if( (cmd.itype < CUSTOM_CMD_ITYPE) || (cmd.itype >= TX19A_last) )
	{
		return "ERROR";
	}

	return Instructions[cmd.itype-CUSTOM_CMD_ITYPE].name;
}

static int flow;

static void TouchArg( op_t &x, int isload )
{
  switch( x.type )
  {
    case o_imm:
      doImmd(cmd.ea);
      if ( isOff(uFlag, x.n) )
        ua_add_off_drefs2(x, dr_O, OOF_SIGNED);
      break;

    case o_near:
      ulong ea = toEA( cmd.cs, x.addr );
      int iscall = Instructions[cmd.itype-CUSTOM_CMD_ITYPE].feature & CF_CALL;
      ua_add_cref( x.offb, ea, iscall ? fl_CN : fl_JN );
      if ( flow && iscall )
        flow = func_does_return(ea);

  }
}

int emu( void )
{
  ulong Feature = Instructions[cmd.itype-CUSTOM_CMD_ITYPE].feature;

  flow = ((Feature & CF_STOP) == 0);

  if( Feature & CF_USE1 )   TouchArg( cmd.Op1, 1 );
  if( Feature & CF_USE2 )   TouchArg( cmd.Op2, 1 );
  if( Feature & CF_JUMP )   QueueMark( Q_jumps, cmd.ea );

  if( flow ) 
  {
	  ua_add_cref( 0, cmd.ea+cmd.size, fl_F );
  }

  return 1;
}


//--------------------------------------------------------------------------
// This callback is called for IDP (processor module) notification events
// Here we extend the processor module to disassemble opcode 0xFF
// (This is a hypothetical example)
// There are 2 approaches for the extensions:
//  A. Quick & dirty
//       you implemented custom_ana and custom_out
//       The first checks if the instruction is valid
//       The second generates its text
//  B. Thourough and clean
//       you implement all callbacks
//       custom_ana fills the 'cmd' structure
//       custom_emu creates all xrefs using ua_add_[cd]ref functions
//       custom_out generates the instruction representation
//         (only if the instruction requires special processing
//          or the processor module can't handle the custom instruction for any reason)
//       custom_outop generates the operand representation (only if the operand requires special processing)
//       custom_mnem returns the instruction mnemonics (without the operands)
// The main difference between these 2 approaches is in the presence of cross-references
// and the amount of special processing required by the new instructions

// The quick & dirty approach
// We just produce the instruction mnemonics along with its operands
// No cross-references are created. No special processing.
static int dirty_extension_callback(void * /*user_data*/, int event_id, va_list va)
{
  switch ( event_id )
  {
    case processor_t::custom_ana:
      {
        ea = cmd.ea;
        int length = ana();
        if ( length )
        {
          cmd.size = length;
          return length+1;       // event processed
        }
      }
      break;
    case processor_t::custom_emu:
      if ( cmd.itype >= CUSTOM_CMD_ITYPE )
      {
		emu();
        return 2;
      }
      break;
    case processor_t::custom_mnem:
      if ( cmd.itype >= CUSTOM_CMD_ITYPE )
      {
        char *buf   = va_arg(va, char *);
        size_t size = va_arg(va, size_t);
        qstrncpy(buf, get_insn_mnem(), size);
        return 2;
      }
      break;
  }
  return 0;                     // event is not processed
}

//--------------------------------------------------------------------------
//
//      Initialize.
//
//      IDA will call this function only once.
//      If this function returns PLGUIN_SKIP, IDA will never load it again.
//      If this function returns PLUGIN_OK, IDA will unload the plugin but
//      remember that the plugin agreed to work with the database.
//      The plugin will be loaded again if the user invokes it by
//      pressing the hotkey or selecting it from the menu.
//      After the second load the plugin will stay on memory.
//      If this function returns PLUGIN_KEEP, IDA will keep the plugin
//      in the memory. In this case the initialization function can hook
//      into the processor module and user interface notification points.
//      See the hook_to_notification_point() function.
//
//      In this example we check the processor type and make the decision.
//      You may or may not check any other conditions to decide what you do:
//      whether you agree to work with the database or not.
//

static bool hooked = false;
static netnode tx19a_node;
static const char node_name[] = "$ Toshiba TX19A processor extender parameters";

int init(void)
{
	if ( ph.id != PLFM_MIPS ) return PLUGIN_SKIP;
	tx19a_node.create(node_name);
	hooked = tx19a_node.altval(0);

	mips16_segRegIndex = -1;

	for( int i = 0 ; i < ph.regsNum ; ++i )
	{
		const char *name = ph.regNames[i];

		if( !strcmp( name, "mips16" ) )
		{
			mips16_segRegIndex = i;
			break;
		}
	}

	if ( hooked )
	{
		hook_to_notification_point(HT_IDP, dirty_extension_callback, NULL);
		msg("Toshiba TX19A processor extender is enabled\n");
		return PLUGIN_KEEP;
	}
	return PLUGIN_OK;
}

//--------------------------------------------------------------------------
//      Terminate.
//      Usually this callback is empty.
//      The plugin should unhook from the notification lists if
//      hook_to_notification_point() was used.
//
//      IDA will call this function when the user asks to exit.
//      This function won't be called in the case of emergency exits.

void term(void)
{
  unhook_from_notification_point(HT_IDP, dirty_extension_callback);
}

//--------------------------------------------------------------------------
//
//      The plugin method
//
//      This is the main function of plugin.
//
//      It will be called when the user selects the plugin.
//
//              arg - the input argument, it can be specified in
//                    plugins.cfg file. The default is zero.
//
//

void run(int /*arg*/)
{
  if ( hooked )
    unhook_from_notification_point(HT_IDP, dirty_extension_callback);
  else
    hook_to_notification_point(HT_IDP, dirty_extension_callback, NULL);
  hooked = !hooked;
  tx19a_node.create(node_name);
  tx19a_node.altset(0, hooked);
  info("AUTOHIDE NONE\n"
       "Toshiba TX19A processor extender now is %s", hooked ? "enabled" : "disabled");
}

//--------------------------------------------------------------------------
char comment[] = "Toshiba TX19A processor extender";

char help[] =
        "Toshiba TX19A processor extension plugin module\n";


//--------------------------------------------------------------------------
// This is the preferred name of the plugin module in the menu system
// The preferred name may be overriden in plugins.cfg file

char wanted_name[] = "Toshiba TX19A processor extender";


// This is the preferred hotkey for the plugin module
// The preferred hotkey may be overriden in plugins.cfg file
// Note: IDA won't tell you if the hotkey is not correct
//       It will just disable the hotkey.

char wanted_hotkey[] = "";


//--------------------------------------------------------------------------
//
//      PLUGIN DESCRIPTION BLOCK
//
//--------------------------------------------------------------------------
plugin_t PLUGIN =
{
  IDP_INTERFACE_VERSION,
  PLUGIN_PROC,          // plugin flags
  init,                 // initialize

  term,                 // terminate. this pointer may be NULL.

  run,                  // invoke plugin

  comment,              // long comment about the plugin
                        // it could appear in the status line
                        // or as a hint

  help,                 // multiline help about the plugin

  wanted_name,          // the preferred short name of the plugin
  wanted_hotkey         // the preferred hotkey to run the plugin
};
