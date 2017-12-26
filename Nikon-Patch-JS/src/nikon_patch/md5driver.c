/*
 **********************************************************************
 ** md5driver.c -- sample routines to test                           **
 ** RSA Data Security, Inc. MD5 message digest algorithm.            **
 ** Created: 2/16/90 RLR                                             **
 ** Updated: 1/91 SRD                                                **
 **********************************************************************
 */

/*
 **********************************************************************
 ** Copyright (C) 1990, RSA Data Security, Inc. All rights reserved. **
 **                                                                  **
 ** RSA Data Security, Inc. makes no representations concerning      **
 ** either the merchantability of this software or the suitability   **
 ** of this software for any particular purpose.  It is provided "as **
 ** is" without express or implied warranty of any kind.             **
 **                                                                  **
 ** These notices must be retained in any copies of any part of this **
 ** documentation and/or software.                                   **
 **********************************************************************
 */

#include <stdio.h>
#include <sys/types.h>
#include <time.h>
#include <string.h>
#include "md5.h"

/* Prints message digest buffer in mdContext as 32 hexadecimal digits.
   Order is from low-order byte to high-order byte of digest.
   Each byte is printed with high-order hexadecimal digit first.
 */
void MDPrint (MD5_CTX* mdContext)
{
  for (int i = 0; i < 16; i++)
    printf ("%02x", mdContext->digest[i]);
}


// /* Computes the message digest for string inString.
   // Prints out message digest, a space, the string (in quotes) and a
   // carriage return.
 // */
// static void MDString (char *inString)
// {
  // MD5_CTX mdContext;
  // unsigned int len = strlen (inString);

  // MD5Init (&mdContext);
  // MD5Update (&mdContext, inString, len);
  // MD5Final (&mdContext);
  // MDPrint (&mdContext);
  // printf (" \"%s\"\n\n", inString);
// }

// void main (argc, argv)
// int argc;
// char *argv[];
// {

// }

