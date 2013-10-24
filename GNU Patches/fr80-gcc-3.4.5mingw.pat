diff -rup --strip-trailing-cr /c/gcc-3.4.5-20060117-2/gcc/config/fr30/fr30.h /src/gcc-3.4.5-20060117-2/gcc/config/fr30/fr30.h
--- /c/gcc-3.4.5-20060117-2/gcc/config/fr30/fr30.h	2004-03-09 04:00:15 +0100
+++ /src/gcc-3.4.5-20060117-2/gcc/config/fr30/fr30.h	2013-10-24 17:03:31 +0200
@@ -33,9 +33,39 @@ Boston, MA 02111-1307, USA.  */
 /*}}}*/ 
 /*{{{  Run-time target specifications.  */ 
 
+/* Additional flags for the preprocessor.  */
+#define CPP_CPU_SPEC "%{mfr30:-U__FR80__ -U__fr80__} \
+%{mfr80:-D__FR80__ -D__fr80__} \
+ "
+
+/* Assembler switches.  */
+#define ASM_CPU_SPEC \
+"%{mfr30} %{mfr80}"
+
 #undef  ASM_SPEC
-#define ASM_SPEC "%{v}"
+#define ASM_SPEC "%{v} %(asm_cpu)"
+
+#undef  CPP_SPEC
+#define CPP_SPEC "%(cpp_cpu)"
+
+/* Extra machine dependent switches.  */
+#define SUBTARGET_SWITCHES							\
+    { "fr80",			TARGET_FR80_MASK, "Compile for the fr80" },	\
+    { "fr30",			-(TARGET_FR80_MASK), "" },
+
+#define EXTRA_SPECS							\
+  { "asm_cpu",			ASM_CPU_SPEC },				\
+  { "cpp_cpu",			CPP_CPU_SPEC }
+
+/* Define this macro as a C expression for the initializer of an array of
+   strings to tell the driver program which options are defaults for this
+   target and thus do not need to be handled specially when using
+   `MULTILIB_OPTIONS'.  */
+#define SUBTARGET_MULTILIB_DEFAULTS "mfr30"
 
+#ifndef MULTILIB_DEFAULTS
+#define MULTILIB_DEFAULTS { SUBTARGET_MULTILIB_DEFAULTS }
+#endif
 /* Define this to be a string constant containing `-D' options to define the
    predefined macros that identify this machine and system.  These macros will
    be predefined unless the `-ansi' option is specified. */
@@ -52,6 +82,12 @@ Boston, MA 02111-1307, USA.  */
 #define TARGET_SMALL_MODEL_MASK	(1 << 0)
 #define TARGET_SMALL_MODEL	(target_flags & TARGET_SMALL_MODEL_MASK)
 
+/* Support extended instruction set of m32r2.  */
+#define TARGET_FR80_MASK       (1 << 1)
+#define TARGET_FR80            (target_flags & TARGET_FR80_MASK)
+#undef  TARGET_FR30
+#define TARGET_FR30             (! TARGET_FR80)
+
 #define TARGET_DEFAULT		0
 
 /* This declaration should be present.  */
@@ -63,10 +99,11 @@ extern int target_flags;
     N_("Assume small address space") },				\
   { "no-small-model", - TARGET_SMALL_MODEL_MASK, "" },		\
   { "no-lsim",          0, "" },				\
+  SUBTARGET_SWITCHES							\
   { "",                 TARGET_DEFAULT, "" }			\
 }
 
-#define TARGET_VERSION fprintf (stderr, " (fr30)");
+#define TARGET_VERSION fprintf (stderr, " (fr30/fr80)");
 
 #define CAN_DEBUG_WITHOUT_FP
 
diff -rup --strip-trailing-cr /c/gcc-3.4.5-20060117-2/gcc/config/fr30/t-fr30 /src/gcc-3.4.5-20060117-2/gcc/config/fr30/t-fr30
--- /c/gcc-3.4.5-20060117-2/gcc/config/fr30/t-fr30	2001-05-17 05:16:18 +0200
+++ /src/gcc-3.4.5-20060117-2/gcc/config/fr30/t-fr30	2013-10-23 15:06:40 +0200
@@ -2,11 +2,11 @@ LIB1ASMSRC    = fr30/lib1funcs.asm
 LIB1ASMFUNCS  = _udivsi3 _divsi3 _umodsi3 _modsi3
 
 # Assemble startup files.
-crti.o: $(srcdir)/config/fr30/crti.asm $(GCC_PASSES)
-	$(GCC_FOR_TARGET) -c -o crti.o -x assembler $(srcdir)/config/fr30/crti.asm
+$(T)crti.o: $(srcdir)/config/fr30/crti.asm $(GCC_PASSES)
+	$(GCC_FOR_TARGET) -c $(MULTILIB_CFLAGS) -o $(T)crti.o -x assembler $(srcdir)/config/fr30/crti.asm
 
-crtn.o: $(srcdir)/config/fr30/crtn.asm $(GCC_PASSES)
-	$(GCC_FOR_TARGET) -c -o crtn.o -x assembler $(srcdir)/config/fr30/crtn.asm
+$(T)crtn.o: $(srcdir)/config/fr30/crtn.asm $(GCC_PASSES)
+	$(GCC_FOR_TARGET) -c $(MULTILIB_CFLAGS) -o $(T)crtn.o -x assembler $(srcdir)/config/fr30/crtn.asm
 
 # We want fine grained libraries, so use the new code to build the
 # floating point emulation libraries.
@@ -36,3 +36,12 @@ dp-bit.c: $(srcdir)/config/fp-bit.c
 #
 # LIBGCC = stmp-multilib
 # INSTALL_LIBGCC = install-multilib
+MULTILIB_OPTIONS     = mfr30/mfr80
+MULTILIB_DIRNAMES    = fr30 fr80
+MULTILIB_EXCEPTIONS  = 
+MULTILIB_MATCHES     =
+
+EXTRA_MULTILIB_PARTS = crtbegin.o crtend.o crti.o crtn.o
+
+LIBGCC = stmp-multilib
+INSTALL_LIBGCC = install-multilib
