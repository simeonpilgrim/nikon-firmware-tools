diff -rup --strip-trailing-cr /c/binutils-2.24.51/bfd/archures.c /src/binutils-2.24.51/bfd/archures.c
--- /c/binutils-2.24.51/bfd/archures.c	2013-08-27 00:18:06 +0200
+++ /src/binutils-2.24.51/bfd/archures.c	2013-10-12 02:22:13 +0200
@@ -352,6 +352,7 @@ DESCRIPTION
 .#define bfd_mach_am33_2	332
 .  bfd_arch_fr30,
 .#define bfd_mach_fr30		0x46523330
+.#define bfd_mach_fr80		0x46523830
 .  bfd_arch_frv,
 .#define bfd_mach_frv		1
 .#define bfd_mach_frvsimple	2
diff -rup --strip-trailing-cr /c/binutils-2.24.51/bfd/bfd-in2.h /src/binutils-2.24.51/bfd/bfd-in2.h
--- /c/binutils-2.24.51/bfd/bfd-in2.h	2013-10-09 16:26:26 +0200
+++ /src/binutils-2.24.51/bfd/bfd-in2.h	2013-10-12 02:21:36 +0200
@@ -2109,6 +2109,7 @@ enum bfd_architecture
 #define bfd_mach_am33_2        332
   bfd_arch_fr30,
 #define bfd_mach_fr30          0x46523330
+#define bfd_mach_fr80          0x46523830
   bfd_arch_frv,
 #define bfd_mach_frv           1
 #define bfd_mach_frvsimple     2
diff -rup --strip-trailing-cr /c/binutils-2.24.51/bfd/cpu-fr30.c /src/binutils-2.24.51/bfd/cpu-fr30.c
--- /c/binutils-2.24.51/bfd/cpu-fr30.c	2012-01-31 18:54:35 +0100
+++ /src/binutils-2.24.51/bfd/cpu-fr30.c	2013-10-12 01:56:21 +0200
@@ -22,19 +22,16 @@
 #include "bfd.h"
 #include "libbfd.h"
 
-const bfd_arch_info_type bfd_fr30_arch =
+#define N(number, print, default, next)  \
+{  32, 32, 8, bfd_arch_fr30, number, "fr30", print, 4, default, \
+     bfd_default_compatible, bfd_default_scan, bfd_arch_default_fill, next }
+
+#define NEXT         & arch_info_struct [0]
+
+static const bfd_arch_info_type arch_info_struct[] =
 {
-  32,				/* bits per word */
-  32,				/* bits per address */
-  8,				/* bits per byte */
-  bfd_arch_fr30,		/* architecture */
-  bfd_mach_fr30,		/* machine */
-  "fr30",			/* architecture name */
-  "fr30",			/* printable name */
-  4,				/* section align power */
-  TRUE,				/* the default ? */
-  bfd_default_compatible,	/* architecture comparison fn */
-  bfd_default_scan,		/* string to architecture convert fn */
-  bfd_arch_default_fill,	/* Default fill.  */
-  NULL				/* next in list */
+  N (bfd_mach_fr80, "fr80", FALSE, NULL)
 };
+
+const bfd_arch_info_type bfd_fr30_arch =
+  N (bfd_mach_fr30, "fr30", TRUE, NEXT);
diff -rup --strip-trailing-cr /c/binutils-2.24.51/bfd/elf32-fr30.c /src/binutils-2.24.51/bfd/elf32-fr30.c
--- /c/binutils-2.24.51/bfd/elf32-fr30.c	2013-03-30 11:14:14 +0100
+++ /src/binutils-2.24.51/bfd/elf32-fr30.c	2013-10-12 02:08:05 +0200
@@ -614,6 +614,140 @@ fr30_elf_relocate_section (bfd *output_b
   return TRUE;
 }
 
+/* Set the right machine number.  */
+
+static bfd_boolean
+fr30_elf_object_p (bfd *abfd)
+{
+  switch (elf_elfheader (abfd)->e_flags & EF_FR30_ARCH)
+    {
+    default:
+    case E_FR30_ARCH:
+        (void) bfd_default_set_arch_mach (abfd, bfd_arch_fr30, bfd_mach_fr30);
+        break;
+    case E_FR80_ARCH:
+        (void) bfd_default_set_arch_mach (abfd, bfd_arch_fr30, bfd_mach_fr80);
+        break;
+    }
+  return TRUE;
+}
+
+/* Store the machine number in the flags field.  */
+
+static void
+fr30_elf_final_write_processing (bfd *abfd,
+				 bfd_boolean linker ATTRIBUTE_UNUSED)
+{
+  elf_elfheader (abfd)->e_flags &= (~EF_FR30_ARCH);
+  elf_elfheader (abfd)->e_flags |=
+                fr30_elf_get_flags_from_mach (bfd_get_mach (abfd));
+}
+
+/* Function to keep FR30 specific file flags.  */
+
+static bfd_boolean
+fr30_elf_set_private_flags (bfd *abfd, flagword flags)
+{
+  BFD_ASSERT (!elf_flags_init (abfd)
+	      || elf_elfheader (abfd)->e_flags == flags);
+
+  elf_elfheader (abfd)->e_flags = flags;
+  elf_flags_init (abfd) = TRUE;
+  return TRUE;
+}
+
+/* Merge backend specific data from an object file to the output
+   object file when linking.  */
+
+static bfd_boolean
+fr30_elf_merge_private_bfd_data (bfd *ibfd, bfd *obfd)
+{
+  flagword out_flags;
+  flagword in_flags;
+
+  if (   bfd_get_flavour (ibfd) != bfd_target_elf_flavour
+      || bfd_get_flavour (obfd) != bfd_target_elf_flavour)
+    return TRUE;
+
+  in_flags  = elf_elfheader (ibfd)->e_flags;
+  out_flags = elf_elfheader (obfd)->e_flags;
+
+  if (! elf_flags_init (obfd))
+    {
+      /* If the input is the default architecture then do not
+	 bother setting the flags for the output architecture,
+	 instead allow future merges to do this.  If no future
+	 merges ever set these flags then they will retain their
+	 unitialised values, which surprise surprise, correspond
+	 to the default values.  */
+      if (bfd_get_arch_info (ibfd)->the_default)
+	return TRUE;
+
+      elf_flags_init (obfd) = TRUE;
+      elf_elfheader (obfd)->e_flags = in_flags;
+
+      if (bfd_get_arch (obfd) == bfd_get_arch (ibfd)
+	  && bfd_get_arch_info (obfd)->the_default)
+	return bfd_set_arch_mach (obfd, bfd_get_arch (ibfd),
+				  bfd_get_mach (ibfd));
+
+      return TRUE;
+    }
+
+  /* Check flag compatibility.  */
+  if (in_flags == out_flags)
+    return TRUE;
+
+  if ((in_flags & EF_FR30_ARCH) != (out_flags & EF_FR30_ARCH))
+    {
+	  (*_bfd_error_handler)
+	    (_("%B: Instruction set mismatch with previous modules"), ibfd);
+
+	  bfd_set_error (bfd_error_bad_value);
+	  return FALSE;
+    }
+
+  return TRUE;
+}
+
+/* Display the flags field.  */
+
+static bfd_boolean
+fr30_elf_print_private_bfd_data (bfd *abfd, void * ptr)
+{
+  FILE * file = (FILE *) ptr;
+
+  BFD_ASSERT (abfd != NULL && ptr != NULL);
+
+  _bfd_elf_print_private_bfd_data (abfd, ptr);
+
+  fprintf (file, _("private flags = %lx"), elf_elfheader (abfd)->e_flags);
+
+  switch (elf_elfheader (abfd)->e_flags & EF_FR30_ARCH)
+    {
+    default:
+    case E_FR30_ARCH:  fprintf (file, _(": fr30 instructions"));  break;
+    case E_FR80_ARCH: fprintf (file, _(": fr80 instructions")); break;
+    }
+
+  fputc ('\n', file);
+
+  return TRUE;
+}
+
+int fr30_elf_get_flags_from_mach (unsigned long mach)
+{
+  int val;
+
+  switch (mach)
+    {
+    default:
+    case bfd_mach_fr30: val = E_FR30_ARCH; break;
+    case bfd_mach_fr80: val = E_FR80_ARCH; break;
+    }
+   return val;
+}
+
 /* Return the section that should be marked against GC for a given
    relocation.  */
 
@@ -717,6 +851,12 @@ fr30_elf_check_relocs (bfd *abfd,
 #define elf_backend_can_gc_sections		1
 #define elf_backend_rela_normal			1
 
+#define elf_backend_object_p			fr30_elf_object_p
+#define elf_backend_final_write_processing 	fr30_elf_final_write_processing
+#define bfd_elf32_bfd_merge_private_bfd_data 	fr30_elf_merge_private_bfd_data
+#define bfd_elf32_bfd_set_private_flags		    fr30_elf_set_private_flags
+#define bfd_elf32_bfd_print_private_bfd_data	fr30_elf_print_private_bfd_data
+
 #define bfd_elf32_bfd_reloc_type_lookup		fr30_reloc_type_lookup
 #define bfd_elf32_bfd_reloc_name_lookup	fr30_reloc_name_lookup
 
diff -rup --strip-trailing-cr /c/binutils-2.24.51/cpu/fr30.cpu /src/binutils-2.24.51/cpu/fr30.cpu
--- /c/binutils-2.24.51/cpu/fr30.cpu	2011-08-22 17:25:07 +0200
+++ /src/binutils-2.24.51/cpu/fr30.cpu	2013-10-11 16:28:03 +0200
@@ -31,7 +31,7 @@
   (comment "Fujitsu FR30")
   (default-alignment forced)
   (insn-lsb0? #f)
-  (machs fr30)
+  (machs fr30 fr80)
   (isas fr30)
 )
 
@@ -53,11 +53,27 @@
   (word-bitsize 32)
 )
 
+(define-cpu
+  ; cpu names must be distinct from the architecture name and machine names.
+  ; The "b" suffix stands for "base" and is the convention.
+  ; The "f" suffix stands for "family" and is the convention.
+  (name fr80f)
+  (comment "Fujitsu FR80 family")
+  (endian big)
+  (word-bitsize 32)
+)
+
 (define-mach
   (name fr30)
   (comment "Generic FR30 cpu")
   (cpu fr30bf)
 )
+
+(define-mach
+  (name fr80)
+  (comment "FR80 cpu")
+  (cpu fr80f)
+)
 
 ; Model descriptions.
 ;
@@ -120,6 +136,66 @@
 	() ; profile action (default)
 	)
 )
+
+(define-model
+  (name fr80) (comment "fr80") (attrs)
+  (mach fr80)
+
+  (pipeline all "" () ((fetch) (decode) (execute) (writeback)))
+
+  ; `state' is a list of variables for recording model state
+  (state
+   ; bit mask of h-gr registers loaded from memory by previous insn
+   (load-regs UINT)
+   ; bit mask of h-gr registers loaded from memory by current insn
+   (load-regs-pending UINT)
+   )
+
+  (unit u-exec "Execution Unit" ()
+	1 1 ; issue done
+	() ; state
+	((Ri INT -1) (Rj INT -1)) ; inputs
+	((Ri INT -1)) ; outputs
+	() ; profile action (default)
+	)
+  (unit u-cti "Branch Unit" ()
+	1 1 ; issue done
+	() ; state
+	((Ri INT -1)) ; inputs
+	((pc)) ; outputs
+	() ; profile action (default)
+	)
+  (unit u-load "Memory Load Unit" ()
+	1 1 ; issue done
+	() ; state
+	((Rj INT -1)
+	 ;(ld-mem AI)
+	 ) ; inputs
+	((Ri INT -1)) ; outputs
+	() ; profile action (default)
+	)
+  (unit u-store "Memory Store Unit" ()
+	1 1 ; issue done
+	() ; state
+	((Ri INT -1) (Rj INT -1)) ; inputs
+	() ; ((st-mem AI)) ; outputs
+	() ; profile action (default)
+	)
+  (unit u-ldm "LDM Memory Load Unit" ()
+	1 1 ; issue done
+	() ; state
+	((reglist INT)) ; inputs
+	() ; outputs
+	() ; profile action (default)
+	)
+  (unit u-stm "STM Memory Store Unit" ()
+	1 1 ; issue done
+	() ; state
+	((reglist INT)) ; inputs
+	() ; outputs
+	() ; profile action (default)
+	)
+)
 
 ; The instruction fetch/execute cycle.
 ;
@@ -1562,7 +1638,7 @@
 ;
 (dni ldres
      "ldres @Ri+,#u4"
-     ()
+     ((MACH fr30))
      "ldres @$Ri+,$u4"
      (+ OP1_B OP2_C u4 Ri)
      (set Ri (add Ri (const 4)))
@@ -1571,7 +1647,7 @@
 
 (dni stres
      "stres #u4,@Ri+"
-     ()
+     ((MACH fr30))
      "stres $u4,@$Ri+"
      (+ OP1_B OP2_D u4 Ri)
      (set Ri (add Ri (const 4)))
@@ -1583,7 +1659,7 @@
 (define-pmacro (cop-stub name insn opc1 opc2 opc3 arg1 arg2)
   (dni name
        (.str insn " u4c,ccc,CRj," arg1 "," arg2)
-       (NOT-IN-DELAY-SLOT)
+       (NOT-IN-DELAY-SLOT (MACH fr30))
        (.str insn " $u4c,$ccc,$" arg1 ",$" arg2)
        (+ opc1 opc2 opc3 u4c ccc arg1 arg2)
        (nop) ; STUB
@@ -1859,3 +1935,21 @@
 	       (set (mem UQI Rj) tmp))
      ((fr30-1 (unit u-load) (unit u-store)))
 )
+
+; FIXME: bit search insns as stubs for now, because of lack integer-length
+; function in rtx.
+;
+(define-pmacro (srch-stub name opc1 opc2 opc3)
+  (dni name
+       (.str name " Ri")
+       ((MACH fr80))
+       (.str name " $Ri")
+       (+ opc1 opc2 opc3 Ri)
+       (nop) ; STUB
+       ()
+  )
+)
+
+(srch-stub srch0 OP1_9 OP2_7 OP3_C)
+(srch-stub srch1 OP1_9 OP2_7 OP3_D)
+(srch-stub srchc OP1_9 OP2_7 OP3_E)
diff -rup --strip-trailing-cr /c/binutils-2.24.51/cpu/fr30.opc /src/binutils-2.24.51/cpu/fr30.opc
--- /c/binutils-2.24.51/cpu/fr30.opc	2011-08-22 17:25:07 +0200
+++ /src/binutils-2.24.51/cpu/fr30.opc	2013-10-11 16:10:10 +0200
@@ -37,14 +37,36 @@
 
 /* -- opc.h */
 
+/* Check applicability of instructions against machines.  */
+#define CGEN_VALIDATE_INSN_SUPPORTED
+
 /* ??? This can be improved upon.  */
 #undef  CGEN_DIS_HASH_SIZE
 #define CGEN_DIS_HASH_SIZE 16
 #undef  CGEN_DIS_HASH
 #define CGEN_DIS_HASH(buffer, value) (((unsigned char *) (buffer))[0] >> 4)
 
+extern int fr30_cgen_insn_supported (CGEN_CPU_DESC, const CGEN_INSN *);
+
 /* -- */
 
+/* -- opc.c */
+
+/* Special check to ensure that instruction exists for given machine.  */
+
+int
+fr30_cgen_insn_supported (CGEN_CPU_DESC cd, const CGEN_INSN *insn)
+{
+  int machs = CGEN_INSN_ATTR_VALUE (insn, CGEN_INSN_MACH);
+
+  /* No mach attribute?  Assume it's supported for all machs.  */
+  if (machs == 0)
+    return 1;
+
+  return (machs & cd->machs) != 0;
+}
+
+
 /* -- asm.c */
 /* Handle register lists for LDMx and STMx.  */
 
diff -rup --strip-trailing-cr /c/binutils-2.24.51/gas/config/tc-fr30.c /src/binutils-2.24.51/gas/config/tc-fr30.c
--- /c/binutils-2.24.51/gas/config/tc-fr30.c	2009-07-24 13:45:00 +0200
+++ /src/binutils-2.24.51/gas/config/tc-fr30.c	2013-10-11 16:44:25 +0200
@@ -26,6 +26,7 @@
 #include "opcodes/fr30-desc.h"
 #include "opcodes/fr30-opc.h"
 #include "cgen.h"
+#include "elf/fr30.h"
 
 /* Structure to hold all of the different components describing
    an individual instruction.  */
@@ -54,12 +55,53 @@ const char line_comment_chars[]   = "#";
 const char line_separator_chars[] = "|";
 const char EXP_CHARS[]            = "eE";
 const char FLT_CHARS[]            = "dD";
+
+static int enable_fr80 = 0; /* Default to FR30.  */
+
+/* check data alignment and display problems.  */
+static int enforce_aligned_data = 0;
+
+
+struct
+{
+  enum bfd_architecture bfd_mach;
+  int mach_flags;
+} mach_table[] =
+{
+  { bfd_mach_fr30,  (1<<MACH_FR30) },
+  { bfd_mach_fr80, (1<<MACH_FR80) }
+};
+
+static void
+allow_fr80 (int on)
+{
+  enable_fr80 = on;
+
+  /* these settings are done if option comes in the middle.  */
+  if (stdoutput != NULL)
+    {
+      if (!bfd_set_arch_mach (stdoutput, TARGET_ARCH, mach_table[on].bfd_mach))
+          as_warn (_("could not set architecture and machine"));
+    }
+  if (gas_cgen_cpu_desc != NULL)
+    gas_cgen_cpu_desc->machs = mach_table[on].mach_flags;
+}
 
 #define FR30_SHORTOPTS ""
 const char * md_shortopts = FR30_SHORTOPTS;
 
+enum md_option_enums
+{
+  OPTION_FR30 = OPTION_MD_BASE,
+  OPTION_FR80,
+  OPTION_ENFORCE_ALIGNED_DATA
+};
+
 struct option md_longopts[] =
 {
+  {"mfr30",  no_argument, NULL, OPTION_FR30},
+  {"mfr80", no_argument, NULL, OPTION_FR80},
+  {"enforce-aligned-data", no_argument, NULL, OPTION_ENFORCE_ALIGNED_DATA},
   {NULL, no_argument, NULL, 0}
 };
 size_t md_longopts_size = sizeof (md_longopts);
@@ -70,6 +112,15 @@ md_parse_option (int c ATTRIBUTE_UNUSED,
 {
   switch (c)
     {
+    case OPTION_FR30:
+      allow_fr80 (0);
+      break;
+    case OPTION_FR80:
+      allow_fr80 (1);
+      break;
+    case OPTION_ENFORCE_ALIGNED_DATA:
+      enforce_aligned_data = 1;
+      break;
     default:
       return 0;
     }
@@ -80,12 +131,21 @@ void
 md_show_usage (FILE * stream)
 {
   fprintf (stream, _(" FR30 specific command line options:\n"));
+
+  fprintf (stream, _("\
+  -mfr30                   disable support for the fr80 instruction set\n"));
+  fprintf (stream, _("\
+  -mfr80                   support the fr80 instruction set\n"));
+  fprintf (stream, _("\
+  --enforce-aligned-data   check data placement to be aligned correctly\n"));
 }
 
 /* The target specific pseudo-ops which we support.  */
 const pseudo_typeS md_pseudo_table[] =
 {
   { "word",	cons,		4 },
+  { "mfr30", allow_fr80,	0 },
+  { "mfr80", allow_fr80,	1 },
   { NULL, 	NULL, 		0 }
 };
 
@@ -96,7 +156,8 @@ md_begin (void)
   /* Initialize the `cgen' interface.  */
 
   /* Set the machine number and endian.  */
-  gas_cgen_cpu_desc = fr30_cgen_cpu_open (CGEN_CPU_OPEN_MACHS, 0,
+  gas_cgen_cpu_desc = fr30_cgen_cpu_open (CGEN_CPU_OPEN_MACHS,
+                      mach_table[enable_fr80].mach_flags,
 					  CGEN_CPU_OPEN_ENDIAN,
 					  CGEN_ENDIAN_BIG,
 					  CGEN_CPU_OPEN_END);
@@ -104,6 +165,11 @@ md_begin (void)
 
   /* This is a callback from cgen to gas to parse operands.  */
   cgen_set_parse_operand_fn (gas_cgen_cpu_desc, gas_cgen_parse_operand);
+
+  /* stdoutput may not exist in md_parse_option () yet, so do it here also.  */
+  if (!bfd_set_arch_mach (stdoutput,
+                          TARGET_ARCH, mach_table[enable_fr80].bfd_mach))
+    as_warn (_("could not set architecture and machine"));
 }
 
 void
@@ -125,6 +191,23 @@ md_assemble (char *str)
       return;
     }
 
+  if (enable_fr80
+       /* FIXME: Need standard macro to perform this test.  */
+      && ((CGEN_INSN_ATTR_VALUE (insn.insn, CGEN_INSN_MACH)
+           & (1 << MACH_FR30))
+          && !((CGEN_INSN_ATTR_VALUE (insn.insn, CGEN_INSN_MACH)
+               & (1 << MACH_FR80)))))
+    {
+      as_bad (_("instruction '%s' is for the FR30 only"), str);
+      return;
+    }
+  else if (! enable_fr80
+	   && CGEN_INSN_ATTR_VALUE (insn.insn, CGEN_INSN_MACH) == (1 << MACH_FR80))
+    {
+      as_bad (_("instruction '%s' is for the FR80 only"), str);
+      return;
+    }
+
   /* Doesn't really matter what we pass for RELAX_P here.  */
   gas_cgen_finish_insn (insn.insn, insn.buffer,
 			CGEN_FIELDS_BITSIZE (& insn.fields), 1, NULL);
@@ -155,6 +238,10 @@ md_operand (expressionS * expressionP)
 valueT
 md_section_align (segT segment, valueT size)
 {
+  /* force 2-byte alignment for code.  */
+  if (subseg_text_p (segment))
+    record_alignment (segment,1);
+
   int align = bfd_get_section_alignment (stdoutput, segment);
 
   return ((size + (1 << align) - 1) & (-1 << align));
@@ -309,6 +396,15 @@ md_cgen_lookup_reloc (const CGEN_INSN *i
 
   return BFD_RELOC_NONE;
 }
+
+void
+fr30_elf_final_processing (void)
+{
+  elf_elfheader (stdoutput)->e_flags &= ~EF_FR30_ARCH;
+  elf_elfheader (stdoutput)->e_flags |=
+                fr30_elf_get_flags_from_mach (mach_table[enable_fr80].bfd_mach);
+}
+
 
 /* Write a value out to the object file, using the appropriate endianness.  */
 
@@ -418,3 +514,41 @@ fr30_fix_adjustable (fixS * fixP)
 
   return 1;
 }
+
+static int fr30_log2 (int value) {
+  int result;
+
+  for (result = 0; (value & 1) == 0; value >>= 1)
+    ++result;
+
+  return result;
+}
+
+void
+fr30_cons_align (int nbytes)
+{
+  int nalign;
+
+  /* Only do this if we are enforcing aligned data.  */
+  if (! enforce_aligned_data)
+    return;
+
+  if (subseg_text_p (now_seg))
+    {
+      /* warn if adding bytes in code segment.  */
+      if (nbytes==1)
+        {
+          as_warn_where (frag_now->fr_file, frag_now->fr_line,
+    	    _("possibly padding of code not a multiple of 2"));
+          return;
+        }
+    }
+
+  /* if it is word or half-word, check alignment.  */
+  if (nbytes!=2 && nbytes!=4)
+    return;
+
+  nalign = fr30_log2 (nbytes);
+  frag_var (rs_align_test, 1, (1<<nalign), (relax_substateT) 0,
+    (symbolS *) NULL, (offsetT) nalign, (char *) NULL);
+}
diff -rup --strip-trailing-cr /c/binutils-2.24.51/gas/config/tc-fr30.h /src/binutils-2.24.51/gas/config/tc-fr30.h
--- /c/binutils-2.24.51/gas/config/tc-fr30.h	2009-09-02 09:24:20 +0200
+++ /src/binutils-2.24.51/gas/config/tc-fr30.h	2013-10-09 00:03:30 +0200
@@ -53,6 +53,12 @@ extern bfd_boolean fr30_fix_adjustable (
 #define MD_PCREL_FROM_SECTION(FIX, SEC) md_pcrel_from_section (FIX, SEC)
 extern long md_pcrel_from_section (struct fix *, segT);
 
+#define md_cons_align(nbytes) fr30_cons_align (nbytes)
+extern void fr30_cons_align (int);
+
+#define elf_tc_final_processing 	fr30_elf_final_processing
+extern void fr30_elf_final_processing (void);
+
 /* For 8 vs 16 vs 32 bit branch selection.  */
 #define TC_GENERIC_RELAX_TABLE md_relax_table
 extern const struct relax_type md_relax_table[];
diff -rup --strip-trailing-cr /c/binutils-2.24.51/include/elf/fr30.h /src/binutils-2.24.51/include/elf/fr30.h
--- /c/binutils-2.24.51/include/elf/fr30.h	2010-04-15 12:26:08 +0200
+++ /src/binutils-2.24.51/include/elf/fr30.h	2013-10-06 06:44:13 +0200
@@ -39,4 +39,14 @@ START_RELOC_NUMBERS (elf_fr30_reloc_type
   RELOC_NUMBER (R_FR30_GNU_VTENTRY, 12)
 END_RELOC_NUMBERS (R_FR30_max)
 
+/* Two bit fr30 architecture field.  */
+#define EF_FR30_ARCH		0x30000000
+/* fr30 code.  */
+#define E_FR30_ARCH		    0x00000000
+/* fr80 code.  */
+#define E_FR80_ARCH         0x10000000
+
+/* Convert bfd_mach_* into EF_FR30*.  */
+int fr30_elf_get_flags_from_mach (unsigned long mach);
+
 #endif /* _ELF_FR30_H */
diff -rup --strip-trailing-cr /c/binutils-2.24.51/opcodes/fr30-desc.c /src/binutils-2.24.51/opcodes/fr30-desc.c
--- /c/binutils-2.24.51/opcodes/fr30-desc.c	2010-02-12 04:25:48 +0100
+++ /src/binutils-2.24.51/opcodes/fr30-desc.c	2013-10-04 19:38:23 +0200
@@ -47,6 +47,7 @@ static const CGEN_ATTR_ENTRY MACH_attr[]
 {
   { "base", MACH_BASE },
   { "fr30", MACH_FR30 },
+  { "fr80", MACH_FR80 },
   { "max", MACH_MAX },
   { 0, 0 }
 };
@@ -123,6 +124,7 @@ static const CGEN_ISA fr30_cgen_isa_tabl
 
 static const CGEN_MACH fr30_cgen_mach_table[] = {
   { "fr30", "fr30", MACH_FR30, 0 },
+  { "fr80", "fr80", MACH_FR80, 0 },
   { 0, 0, 0, 0 }
 };
 
@@ -1296,32 +1298,32 @@ static const CGEN_IBASE fr30_cgen_insn_t
 /* ldres @$Ri+,$u4 */
   {
     FR30_INSN_LDRES, "ldres", "ldres", 16,
-    { 0, { { { (1<<MACH_BASE), 0 } } } }
+    { 0, { { { (1<<MACH_FR30), 0 } } } }
   },
 /* stres $u4,@$Ri+ */
   {
     FR30_INSN_STRES, "stres", "stres", 16,
-    { 0, { { { (1<<MACH_BASE), 0 } } } }
+    { 0, { { { (1<<MACH_FR30), 0 } } } }
   },
 /* copop $u4c,$ccc,$CRj,$CRi */
   {
     FR30_INSN_COPOP, "copop", "copop", 32,
-    { 0|A(NOT_IN_DELAY_SLOT), { { { (1<<MACH_BASE), 0 } } } }
+    { 0|A(NOT_IN_DELAY_SLOT), { { { (1<<MACH_FR30), 0 } } } }
   },
 /* copld $u4c,$ccc,$Rjc,$CRi */
   {
     FR30_INSN_COPLD, "copld", "copld", 32,
-    { 0|A(NOT_IN_DELAY_SLOT), { { { (1<<MACH_BASE), 0 } } } }
+    { 0|A(NOT_IN_DELAY_SLOT), { { { (1<<MACH_FR30), 0 } } } }
   },
 /* copst $u4c,$ccc,$CRj,$Ric */
   {
     FR30_INSN_COPST, "copst", "copst", 32,
-    { 0|A(NOT_IN_DELAY_SLOT), { { { (1<<MACH_BASE), 0 } } } }
+    { 0|A(NOT_IN_DELAY_SLOT), { { { (1<<MACH_FR30), 0 } } } }
   },
 /* copsv $u4c,$ccc,$CRj,$Ric */
   {
     FR30_INSN_COPSV, "copsv", "copsv", 32,
-    { 0|A(NOT_IN_DELAY_SLOT), { { { (1<<MACH_BASE), 0 } } } }
+    { 0|A(NOT_IN_DELAY_SLOT), { { { (1<<MACH_FR30), 0 } } } }
   },
 /* nop */
   {
@@ -1403,6 +1405,21 @@ static const CGEN_IBASE fr30_cgen_insn_t
     FR30_INSN_XCHB, "xchb", "xchb", 16,
     { 0|A(NOT_IN_DELAY_SLOT), { { { (1<<MACH_BASE), 0 } } } }
   },
+/* srch0 $Ri */
+  {
+    FR30_INSN_SRCH0, "srch0", "srch0", 16,
+    { 0, { { { (1<<MACH_FR80), 0 } } } }
+  },
+/* srch1 $Ri */
+  {
+    FR30_INSN_SRCH1, "srch1", "srch1", 16,
+    { 0, { { { (1<<MACH_FR80), 0 } } } }
+  },
+/* srchc $Ri */
+  {
+    FR30_INSN_SRCHC, "srchc", "srchc", 16,
+    { 0, { { { (1<<MACH_FR80), 0 } } } }
+  },
 };
 
 #undef OP
diff -rup --strip-trailing-cr /c/binutils-2.24.51/opcodes/fr30-desc.h /src/binutils-2.24.51/opcodes/fr30-desc.h
--- /c/binutils-2.24.51/opcodes/fr30-desc.h	2010-10-09 08:50:22 +0200
+++ /src/binutils-2.24.51/opcodes/fr30-desc.h	2013-10-04 16:48:35 +0200
@@ -33,6 +33,7 @@ This file is part of the GNU Binutils an
 
 /* Selected cpu families.  */
 #define HAVE_CPU_FR30BF
+#define HAVE_CPU_FR80F
 
 #define CGEN_INSN_LSB0_P 0
 
@@ -126,7 +127,7 @@ typedef enum dr_names {
 
 /* Enum declaration for machine type selection.  */
 typedef enum mach_attr {
-  MACH_BASE, MACH_FR30, MACH_MAX
+  MACH_BASE, MACH_FR30, MACH_FR80, MACH_MAX
 } MACH_ATTR;
 
 /* Enum declaration for instruction set selection.  */
diff -rup --strip-trailing-cr /c/binutils-2.24.51/opcodes/fr30-opc.c /src/binutils-2.24.51/opcodes/fr30-opc.c
--- /c/binutils-2.24.51/opcodes/fr30-opc.c	2010-02-12 04:25:48 +0100
+++ /src/binutils-2.24.51/opcodes/fr30-opc.c	2013-10-11 16:38:35 +0200
@@ -30,6 +30,24 @@ This file is part of the GNU Binutils an
 #include "fr30-opc.h"
 #include "libiberty.h"
 
+/* -- opc.c */
+
+/* Special check to ensure that instruction exists for given machine.  */
+
+int
+fr30_cgen_insn_supported (CGEN_CPU_DESC cd, const CGEN_INSN *insn)
+{
+  int machs = CGEN_INSN_ATTR_VALUE (insn, CGEN_INSN_MACH);
+
+  /* No mach attribute?  Assume it's supported for all machs.  */
+  if (machs == 0)
+    return 1;
+
+  return (machs & cd->machs) != 0;
+}
+
+
+/* -- asm.c */
 /* The hash functions are recorded here to help keep assembler code out of
    the disassembler and vice versa.  */
 
@@ -1166,6 +1184,24 @@ static const CGEN_OPCODE fr30_cgen_insn_
     { { MNEM, ' ', '@', OP (RJ), ',', OP (RI), 0 } },
     & ifmt_add, { 0x8a00 }
   },
+/* srch0 $Ri */
+  {
+    { 0, 0, 0, 0 },
+    { { MNEM, ' ', OP (RI), 0 } },
+    & ifmt_div0s, { 0x97c0 }
+  },
+/* srch1 $Ri */
+  {
+    { 0, 0, 0, 0 },
+    { { MNEM, ' ', OP (RI), 0 } },
+    & ifmt_div0s, { 0x97d0 }
+  },
+/* srchc $Ri */
+  {
+    { 0, 0, 0, 0 },
+    { { MNEM, ' ', OP (RI), 0 } },
+    & ifmt_div0s, { 0x97e0 }
+  },
 };
 
 #undef A
diff -rup --strip-trailing-cr /c/binutils-2.24.51/opcodes/fr30-opc.h /src/binutils-2.24.51/opcodes/fr30-opc.h
--- /c/binutils-2.24.51/opcodes/fr30-opc.h	2010-01-02 19:50:58 +0100
+++ /src/binutils-2.24.51/opcodes/fr30-opc.h	2013-10-05 23:18:35 +0200
@@ -27,12 +27,17 @@ This file is part of the GNU Binutils an
 
 /* -- opc.h */
 
+/* Check applicability of instructions against machines.  */
+#define CGEN_VALIDATE_INSN_SUPPORTED
+
 /* ??? This can be improved upon.  */
 #undef  CGEN_DIS_HASH_SIZE
 #define CGEN_DIS_HASH_SIZE 16
 #undef  CGEN_DIS_HASH
 #define CGEN_DIS_HASH(buffer, value) (((unsigned char *) (buffer))[0] >> 4)
 
+extern int fr30_cgen_insn_supported (CGEN_CPU_DESC, const CGEN_INSN *);
+
 /* -- */
 /* Enum declaration for fr30 instruction types.  */
 typedef enum cgen_insn_type {
@@ -77,14 +82,15 @@ typedef enum cgen_insn_type {
  , FR30_INSN_ORCCR, FR30_INSN_STILM, FR30_INSN_ADDSP, FR30_INSN_EXTSB
  , FR30_INSN_EXTUB, FR30_INSN_EXTSH, FR30_INSN_EXTUH, FR30_INSN_LDM0
  , FR30_INSN_LDM1, FR30_INSN_STM0, FR30_INSN_STM1, FR30_INSN_ENTER
- , FR30_INSN_LEAVE, FR30_INSN_XCHB
+ , FR30_INSN_LEAVE, FR30_INSN_XCHB, FR30_INSN_SRCH0, FR30_INSN_SRCH1
+ , FR30_INSN_SRCHC
 } CGEN_INSN_TYPE;
 
 /* Index of `invalid' insn place holder.  */
 #define CGEN_INSN_INVALID FR30_INSN_INVALID
 
 /* Total number of insns in table.  */
-#define MAX_INSNS ((int) FR30_INSN_XCHB + 1)
+#define MAX_INSNS ((int) FR30_INSN_SRCHC + 1)
 
 /* This struct records data prior to insertion or after extraction.  */
 struct cgen_fields
