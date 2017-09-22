/* -*- c-basic-offset: 4; indent-tabs-mode: nil -*- */
/* ====================================================================
 * Copyright (c) 1996-2004 Carnegie Mellon University.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * This work was supported in part by funding from the Defense Advanced 
 * Research Projects Agency and the National Science Foundation of the 
 * United States of America, and the CMU Sphinx Speech Consortium.
 *
 * THIS SOFTWARE IS PROVIDED BY CARNEGIE MELLON UNIVERSITY ``AS IS'' AND 
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 */

#include <stdio.h>
#include <math.h>
#include <string.h>
#include <stdlib.h>
#include <assert.h>

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#ifdef _MSC_VER
#pragma warning (disable: 4244)
#endif

/**
 * Windows math.h does not contain M_PI
 */
#ifndef M_PI
#define M_PI		3.14159265358979323846	/* pi */
#endif // M_PI

#include "sphinxbase/prim_type.h"
#include "sphinxbase/ckd_alloc.h"
#include "sphinxbase/byteorder.h"
#include "sphinxbase/fixpoint.h"
#include "sphinxbase/fe.h"
#include "sphinxbase/genrand.h"
#include "sphinxbase/err.h"

#include "fe_internal.h"
#include "fe_warp.h"




#include "emu_sys_log.h"
#undef CMU_SPHINX_TEST_PERFORMANCE



#if 0
//(FE_FFT_REAL_ARM_CODE)
//32+13
int	fft_order_shift_table[45] = {
0x00000001,0x00000002,0x00000004,0x00000008,0x00000010,0x00000020,0x00000040,0x00000080,
0x00000100,0x00000200,0x00000400,0x00000800,0x00001000,0x00002000,0x00004000,0x00008000,
0x00010000,0x00020000,0x00040000,0x00080000,0x00100000,0x00200000,0x00400000,0x00800000,
0x01000000,0x02000000,0x04000000,0x08000000,0x10000000,0x20000000,0x40000000,0x80000000,
0,0,0,0,
0,0,0,0,
0,0,0,0,
0
};
#endif


/* Use extra precision for cosines, Hamming window, pre-emphasis
 * coefficient, twiddle factors. */
#ifdef FIXED_POINT
#define FLOAT2COS(x) FLOAT2FIX_ANY(x,30)
#define COSMUL(x,y) FIXMUL_ANY(x,y,30)
#else
#define FLOAT2COS(x) (x)
#define COSMUL(x,y) ((x)*(y))
#endif

#ifdef FIXED_POINT
/* Internal log-addition table for natural log with radix point at 8
 * bits.  Each entry is 256 * log(1 + e^{-n/256}).  This is used in the
 * log-add computation:
 *
 * e^z = e^x + e^y
 * e^z = e^x(1 + e^{y-x})     = e^y(1 + e^{x-y})
 * z   = x + log(1 + e^{y-x}) = y + log(1 + e^{x-y})
 *
 * So when y > x, z = y + logadd_table[-(x-y)]
 *    when x > y, z = x + logadd_table[-(y-x)]
 */
static const unsigned char fe_logadd_table[] = {
177, 177, 176, 176, 175, 175, 174, 174, 173, 173,
172, 172, 172, 171, 171, 170, 170, 169, 169, 168,
168, 167, 167, 166, 166, 165, 165, 164, 164, 163,
163, 162, 162, 161, 161, 161, 160, 160, 159, 159,
158, 158, 157, 157, 156, 156, 155, 155, 155, 154,
154, 153, 153, 152, 152, 151, 151, 151, 150, 150,
149, 149, 148, 148, 147, 147, 147, 146, 146, 145,
145, 144, 144, 144, 143, 143, 142, 142, 141, 141,
141, 140, 140, 139, 139, 138, 138, 138, 137, 137,
136, 136, 136, 135, 135, 134, 134, 134, 133, 133,
132, 132, 131, 131, 131, 130, 130, 129, 129, 129,
128, 128, 128, 127, 127, 126, 126, 126, 125, 125,
124, 124, 124, 123, 123, 123, 122, 122, 121, 121,
121, 120, 120, 119, 119, 119, 118, 118, 118, 117,
117, 117, 116, 116, 115, 115, 115, 114, 114, 114,
113, 113, 113, 112, 112, 112, 111, 111, 110, 110,
110, 109, 109, 109, 108, 108, 108, 107, 107, 107,
106, 106, 106, 105, 105, 105, 104, 104, 104, 103,
103, 103, 102, 102, 102, 101, 101, 101, 100, 100,
100, 99, 99, 99, 98, 98, 98, 97, 97, 97,
96, 96, 96, 96, 95, 95, 95, 94, 94, 94,
93, 93, 93, 92, 92, 92, 92, 91, 91, 91,
90, 90, 90, 89, 89, 89, 89, 88, 88, 88,
87, 87, 87, 87, 86, 86, 86, 85, 85, 85,
85, 84, 84, 84, 83, 83, 83, 83, 82, 82,
82, 82, 81, 81, 81, 80, 80, 80, 80, 79,
79, 79, 79, 78, 78, 78, 78, 77, 77, 77,
77, 76, 76, 76, 75, 75, 75, 75, 74, 74,
74, 74, 73, 73, 73, 73, 72, 72, 72, 72,
71, 71, 71, 71, 71, 70, 70, 70, 70, 69,
69, 69, 69, 68, 68, 68, 68, 67, 67, 67,
67, 67, 66, 66, 66, 66, 65, 65, 65, 65,
64, 64, 64, 64, 64, 63, 63, 63, 63, 63,
62, 62, 62, 62, 61, 61, 61, 61, 61, 60,
60, 60, 60, 60, 59, 59, 59, 59, 59, 58,
58, 58, 58, 58, 57, 57, 57, 57, 57, 56,
56, 56, 56, 56, 55, 55, 55, 55, 55, 54,
54, 54, 54, 54, 53, 53, 53, 53, 53, 52,
52, 52, 52, 52, 52, 51, 51, 51, 51, 51,
50, 50, 50, 50, 50, 50, 49, 49, 49, 49,
49, 49, 48, 48, 48, 48, 48, 48, 47, 47,
47, 47, 47, 47, 46, 46, 46, 46, 46, 46,
45, 45, 45, 45, 45, 45, 44, 44, 44, 44,
44, 44, 43, 43, 43, 43, 43, 43, 43, 42,
42, 42, 42, 42, 42, 41, 41, 41, 41, 41,
41, 41, 40, 40, 40, 40, 40, 40, 40, 39,
39, 39, 39, 39, 39, 39, 38, 38, 38, 38,
38, 38, 38, 37, 37, 37, 37, 37, 37, 37,
37, 36, 36, 36, 36, 36, 36, 36, 35, 35,
35, 35, 35, 35, 35, 35, 34, 34, 34, 34,
34, 34, 34, 34, 33, 33, 33, 33, 33, 33,
33, 33, 32, 32, 32, 32, 32, 32, 32, 32,
32, 31, 31, 31, 31, 31, 31, 31, 31, 31,
30, 30, 30, 30, 30, 30, 30, 30, 30, 29,
29, 29, 29, 29, 29, 29, 29, 29, 28, 28,
28, 28, 28, 28, 28, 28, 28, 28, 27, 27,
27, 27, 27, 27, 27, 27, 27, 27, 26, 26,
26, 26, 26, 26, 26, 26, 26, 26, 25, 25,
25, 25, 25, 25, 25, 25, 25, 25, 25, 24,
24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
23, 23, 22, 22, 22, 22, 22, 22, 22, 22,
22, 22, 22, 22, 21, 21, 21, 21, 21, 21,
21, 21, 21, 21, 21, 21, 21, 20, 20, 20,
20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
19, 19, 19, 19, 19, 19, 19, 19, 19, 19,
19, 19, 19, 19, 18, 18, 18, 18, 18, 18,
18, 18, 18, 18, 18, 18, 18, 18, 18, 17,
17, 17, 17, 17, 17, 17, 17, 17, 17, 17,
17, 17, 17, 17, 16, 16, 16, 16, 16, 16,
16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
16, 15, 15, 15, 15, 15, 15, 15, 15, 15,
15, 15, 15, 15, 15, 15, 15, 15, 14, 14,
14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
14, 14, 14, 14, 14, 14, 14, 13, 13, 13,
13, 13, 13, 13, 13, 13, 13, 13, 13, 13,
13, 13, 13, 13, 13, 13, 13, 12, 12, 12,
12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
12, 12, 12, 12, 12, 12, 12, 12, 12, 11,
11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
11, 11, 11, 10, 10, 10, 10, 10, 10, 10,
10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
10, 10, 10, 10, 10, 10, 10, 10, 10, 9,
9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
9, 9, 9, 9, 9, 9, 9, 9, 8, 8,
8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
7, 7, 7, 7, 7, 7, 7, 7, 6, 6,
6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
6, 5, 5, 5, 5, 5, 5, 5, 5, 5,
5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
5, 5, 5, 4, 4, 4, 4, 4, 4, 4,
4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
4, 4, 4, 4, 4, 4, 4, 4, 3, 3,
3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
3, 3, 3, 3, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
1, 1, 1, 1, 1, 1, 1, 0
};
static const int fe_logadd_table_size = sizeof(fe_logadd_table) / sizeof(fe_logadd_table[0]);

fixed32 fe_log_add(fixed32 x, fixed32 y)
{
    register fixed32 d, r;
	register int radix = DEFAULT_RADIX - 8;

    if (x > y) {
        d = (x - y) >> radix;
        r = x;
    }
    else {
        d = (y - x) >> radix;
        r = y;
    }
    if (d > fe_logadd_table_size - 1)
        return r;
    else {
        r += ((fixed32)fe_logadd_table[d] << radix);
/*
        printf("%d + %d = %d | %f + %f = %f | %f + %f = %f\n",
               x, y, r, FIX2FLOAT(x), FIX2FLOAT(y), FIX2FLOAT(r),
               exp(FIX2FLOAT(x)), exp(FIX2FLOAT(y)), exp(FIX2FLOAT(r)));
*/
        return r;
    }
}

static fixed32
fe_log(float32 x)
{
    if (x <= 0) {
        return MIN_FIXLOG;
    }
    else {
        return FLOAT2FIX(log(x));
    }
}
#endif

static float32
fe_mel(melfb_t *mel, float32 x)
{
    float32 warped = fe_warp_unwarped_to_warped(mel, x);

    return (float32) (2595.0 * log10(1.0 + warped / 700.0));
}

static float32
fe_melinv(melfb_t *mel, float32 x)
{
    float32 warped = (float32) (700.0 * (pow(10.0, x / 2595.0) - 1.0));
    return fe_warp_warped_to_unwarped(mel, warped);
}

int32
fe_build_melfilters(melfb_t *mel_fb)
{
    float32 melmin, melmax, melbw, fftfreq;
    int n_coeffs, i, j;

//创建滤波器，设置滤波参数

    /* Filter coefficient matrix, in flattened form. */
    mel_fb->spec_start = ckd_malloc(mel_fb->num_filters * sizeof(*mel_fb->spec_start));
    mel_fb->filt_start = ckd_malloc(mel_fb->num_filters * sizeof(*mel_fb->filt_start));
    mel_fb->filt_width = ckd_malloc(mel_fb->num_filters * sizeof(*mel_fb->filt_width));

    /* First calculate the widths of each filter. */
    /* Minimum and maximum frequencies in mel scale. */
    melmin = fe_mel(mel_fb, mel_fb->lower_filt_freq);
    melmax = fe_mel(mel_fb, mel_fb->upper_filt_freq);

    /* Width of filters in mel scale */
    melbw = (melmax - melmin) / (mel_fb->num_filters + 1);
    if (mel_fb->doublewide) {
        melmin -= melbw;
        melmax += melbw;
        if ((fe_melinv(mel_fb, melmin) < 0) ||
            (fe_melinv(mel_fb, melmax) > mel_fb->sampling_rate / 2)) {
            E_WARN
                ("Out of Range: low  filter edge = %f (%f)\n",
                 fe_melinv(mel_fb, melmin), 0.0);
            E_WARN
                ("              high filter edge = %f (%f)\n",
                 fe_melinv(mel_fb, melmax), mel_fb->sampling_rate / 2);
            return FE_INVALID_PARAM_ERROR;
        }
    }

    /* DFT point spacing */
    fftfreq = mel_fb->sampling_rate / (float32) mel_fb->fft_size;

    /* Count and place filter coefficients. */
    n_coeffs = 0;
    for (i = 0; i < mel_fb->num_filters; ++i) {
        float32 freqs[3];

        /* Left, center, right frequencies in Hertz */
        for (j = 0; j < 3; ++j) {
            if (mel_fb->doublewide)
                freqs[j] = fe_melinv(mel_fb, (i + j * 2) * melbw + melmin);
            else
                freqs[j] = fe_melinv(mel_fb, (i + j) * melbw + melmin);
            /* Round them to DFT points if requested */
            if (mel_fb->round_filters)
                freqs[j] = ((int)(freqs[j] / fftfreq + 0.5)) * fftfreq;
        }

        /* spec_start is the start of this filter in the power spectrum. */
        mel_fb->spec_start[i] = -1;
        /* There must be a better way... */
        for (j = 0; j < mel_fb->fft_size/2+1; ++j) {
            float32 hz = j * fftfreq;
            if (hz < freqs[0])
                continue;
            else if (hz > freqs[2] || j == mel_fb->fft_size/2) {
                /* filt_width is the width in DFT points of this filter. */
                mel_fb->filt_width[i] = j - mel_fb->spec_start[i];
                /* filt_start is the start of this filter in the filt_coeffs array. */
                mel_fb->filt_start[i] = n_coeffs;
                n_coeffs += mel_fb->filt_width[i];
                break;
            }
            if (mel_fb->spec_start[i] == -1)
                mel_fb->spec_start[i] = j;
        }
    }

    /* Now go back and allocate the coefficient array. */
    mel_fb->filt_coeffs = ckd_malloc(n_coeffs * sizeof(*mel_fb->filt_coeffs));

    /* And now generate the coefficients. */
    n_coeffs = 0;
    for (i = 0; i < mel_fb->num_filters; ++i) {
        float32 freqs[3];

        /* Left, center, right frequencies in Hertz */
        for (j = 0; j < 3; ++j) {
            if (mel_fb->doublewide)
                freqs[j] = fe_melinv(mel_fb, (i + j * 2) * melbw + melmin);
            else
                freqs[j] = fe_melinv(mel_fb, (i + j) * melbw + melmin);
            /* Round them to DFT points if requested */
            if (mel_fb->round_filters)
                freqs[j] = ((int)(freqs[j] / fftfreq + 0.5)) * fftfreq;
        }

        for (j = 0; j < mel_fb->filt_width[i]; ++j) {
            float32 hz, loslope, hislope;

            hz = (mel_fb->spec_start[i] + j) * fftfreq;
            if (hz < freqs[0] || hz > freqs[2]) {
                E_FATAL("Failed to create filterbank, frequency range does not match. "
                        "Sample rate %f, FFT size %d, lowerf %f < freq %f > upperf %f.\n", mel_fb->sampling_rate, mel_fb->fft_size, freqs[2], hz, freqs[0]);
            }
            loslope = (hz - freqs[0]) / (freqs[1] - freqs[0]);
            hislope = (freqs[2] - hz) / (freqs[2] - freqs[1]);
            if (mel_fb->unit_area) {
                loslope *= 2 / (freqs[2] - freqs[0]);
                hislope *= 2 / (freqs[2] - freqs[0]);
            }
            if (loslope < hislope) {
#ifdef FIXED_POINT
                mel_fb->filt_coeffs[n_coeffs] = fe_log(loslope);
#else
                mel_fb->filt_coeffs[n_coeffs] = loslope;
#endif
            }
            else {
#ifdef FIXED_POINT
                mel_fb->filt_coeffs[n_coeffs] = fe_log(hislope);
#else
                mel_fb->filt_coeffs[n_coeffs] = hislope;
#endif
            }
            ++n_coeffs;
        }
    }
    

    return FE_SUCCESS;
}

int32
fe_compute_melcosine(melfb_t * mel_fb)
{

    float64 freqstep;
    int32 i, j;
//lfl 
    mel_fb->mel_cosine =
        (mfcc_t **) ckd_calloc_2d(mel_fb->num_cepstra,
                                  mel_fb->num_filters,
                                  sizeof(mfcc_t));

    freqstep = M_PI / mel_fb->num_filters;
    /* NOTE: The first row vector is actually unnecessary but we leave
     * it in to avoid confusion. */
    for (i = 0; i < mel_fb->num_cepstra; i++) {
        for (j = 0; j < mel_fb->num_filters; j++) {
            float64 cosine;

            cosine = cos(freqstep * i * (j + 0.5));
            mel_fb->mel_cosine[i][j] = FLOAT2COS(cosine);
        }
    }

    /* Also precompute normalization constants for unitary DCT. */
    mel_fb->sqrt_inv_n = FLOAT2COS(sqrt(1.0 / mel_fb->num_filters));
    mel_fb->sqrt_inv_2n = FLOAT2COS(sqrt(2.0 / mel_fb->num_filters));

    /* And liftering weights */
    if (mel_fb->lifter_val) {
        mel_fb->lifter = calloc(mel_fb->num_cepstra, sizeof(*mel_fb->lifter));
        for (i = 0; i < mel_fb->num_cepstra; ++i) {
            mel_fb->lifter[i] = FLOAT2MFCC(1 + mel_fb->lifter_val / 2
                                           * sin(i * M_PI / mel_fb->lifter_val));
        }
    }

    return (0);
}

static void
fe_pre_emphasis(int16 const *in, frame_t * out, int32 len,
                float32 factor, int16 prior)
{
    int i;
#if(CMU_SPHINX_TEST_PERFORMANCE == 1)
//Iava_SysPrintf("--------fe_pre_emphasis  in[0] = %d   in[1] = %d  in[2] = %d   in[3] = %d",in[0],in[1],in[2],in[3]);
#endif

#if defined(FIXED16)
    int16 fxd_alpha = (int16)(factor * 0x8000);
    int32 tmp1, tmp2;

    tmp1 = (int32)in[0] << 15;
    tmp2 = (int32)prior * fxd_alpha;
    out[0] = (int16)((tmp1 - tmp2) >> 15);
    for (i = 1; i < len; ++i) {
        tmp1 = (int32)in[i] << 15;
        tmp2 = (int32)in[i-1] * fxd_alpha;
        out[i] = (int16)((tmp1 - tmp2) >> 15);
    }
#elif defined(FIXED_POINT)
	
#if (FE_PRE_EMPHASIS_ARM_CODE)

	fixed32 fxd_alpha = FLOAT2FIX(factor);
	out[0] = ((fixed32)in[0] << DEFAULT_RADIX) - (prior * fxd_alpha);
	fe_pre_emphasis_fixed_point_arm_code(in,out,len,fxd_alpha);
#else

    fixed32 fxd_alpha = FLOAT2FIX(factor);
    out[0] = ((fixed32)in[0] << DEFAULT_RADIX) - (prior * fxd_alpha);
    for (i = 1; i < len; ++i)
        out[i] = ((fixed32)in[i] << DEFAULT_RADIX)
            - (fixed32)in[i-1] * fxd_alpha;
#endif

#else
    out[0] = (frame_t) in[0] - (frame_t) prior * factor;
    for (i = 1; i < len; i++)
        out[i] = (frame_t) in[i] - (frame_t) in[i-1] * factor;
#endif



#if(CMU_SPHINX_TEST_PERFORMANCE == 1)
//	Iava_SysPrintf("--------fe_pre_emphasis  ou[0] = %d   ou[1] = %d  ou[2] = %d   ou[3] = %d",out[0],out[1],out[2],out[3]);
#endif


}

static void
fe_short_to_frame(int16 const *in, frame_t * out, int32 len)
{
    int i;

#if defined(FIXED16)
    memcpy(out, in, len * sizeof(*out));
#elif defined(FIXED_POINT)
    for (i = 0; i < len; i++)
        out[i] = (int32) in[i] << DEFAULT_RADIX;
#else                           /* FIXED_POINT */
    for (i = 0; i < len; i++)
        out[i] = (frame_t) in[i];
#endif                          /* FIXED_POINT */
}

void
fe_create_hamming(window_t * in, int32 in_len)
{
    int i;

    /* Symmetric, so we only create the first half of it. */
    for (i = 0; i < in_len / 2; i++) {
        float64 hamm;
        hamm  = (0.54 - 0.46 * cos(2 * M_PI * i /
                                   ((float64) in_len - 1.0)));
#ifdef FIXED16
        in[i] = (int16)(hamm * 0x8000);
#else
        in[i] = FLOAT2COS(hamm);
#endif
    }
}

#if(FE_HAMMING_WINDOW_ARM_CODE)
int get_mean(int mean,int in_len)
{
	frame_t ret = 0;

	ret = mean / in_len;
	
	return ret;
}

static void
fe_hamming_window_debug(frame_t * in, window_t * window, int32 in_len, int32 remove_dc)
{
    int i;


	if (remove_dc) 
	{
		frame_t mean = 0;

		mean = fe_hamming_window_0_arm_code(in,in_len);
		//for (i = 0; i < in_len; i++)
		//	mean += in[i];

		mean /= in_len;

		fe_hamming_window_1_arm_code(in,in_len,mean);
		//for (i = 0; i < in_len; i++)
		//	in[i] -= (frame_t)mean;
	}



	fe_hamming_window_2_arm_code(in,in_len, window);

//    for (i = 0; i < in_len/2; i++) {
//       in[i] = COSMUL(in[i], window[i]);
//        in[in_len-1-i] = COSMUL(in[in_len-1-i], window[i]);
//    }

}

#endif

static void
fe_hamming_window(frame_t * in, window_t * window, int32 in_len, int32 remove_dc)
{
    int i;




#ifdef FIXED16

	if (remove_dc) {
		int32 mean = 0; /* Use int32 to avoid possibility of overflow */

		for (i = 0; i < in_len; i++)
			mean += in[i];
		mean /= in_len;
		for (i = 0; i < in_len; i++)
			in[i] -= (frame_t)mean;
	}


    for (i = 0; i < in_len/2; i++) {
        int32 tmp1, tmp2;

        tmp1 = (int32)in[i] * window[i];
        tmp2 = (int32)in[in_len-1-i] * window[i];
        in[i] = (int16)(tmp1 >> 15);
        in[in_len-1-i] = (int16)(tmp2 >> 15);
    }
#else

	if (remove_dc) {
		frame_t mean = 0;


		for (i = 0; i < in_len; i++)
			mean += in[i];


		mean /= in_len;


		for (i = 0; i < in_len; i++)
			in[i] -= (frame_t)mean;
	}



    for (i = 0; i < in_len/2; i++) {
        in[i] = COSMUL(in[i], window[i]);
        in[in_len-1-i] = COSMUL(in[in_len-1-i], window[i]);
    }
#endif




}

static int
fe_spch_to_frame(fe_t *fe, int len)
{

	register int16 *p_spch = fe->spch;
	register frame_t *p_frame = fe->frame;
#if 0
//(CMU_SPHINX_TEST_PERFORMANCE == 1)
		Iava_SysPrintf("--------fe_spch_to_frame 000 fe->pre_emphasis_alpha = %f!!!",fe->pre_emphasis_alpha);
		Iava_SysPrintf("--------fe_t = 0x%x fe->frame_rate = 0x%x!!!",fe,&(fe->frame_rate));
		Iava_SysPrintf("--------fe_t = 0x%x fe->frame_size = 0x%x!!!",fe,&(fe->frame_size));
		Iava_SysPrintf("--------fe_t = 0x%x fe->seed = 0x%x!!!",fe,&(fe->seed));
		Iava_SysPrintf("--------fe_t = 0x%x fe->ccc = 0x%x!!!",fe,&(fe->ccc));
		Iava_SysPrintf("--------fe_t = 0x%x fe->sss = 0x%x!!!",fe,&(fe->sss));
		Iava_SysPrintf("--------fe_t = 0x%x fe->spec = 0x%x!!!",fe,&(fe->spec));
		Iava_SysPrintf("--------fe_t = 0x%x fe->prior = 0x%x!!!",fe,&(fe->prior));
#endif

    /* Copy to the frame buffer. */
    if (fe->pre_emphasis_alpha != 0.0) 
	{
        fe_pre_emphasis(p_spch, p_frame, len,
                        fe->pre_emphasis_alpha, fe->prior);
        if (len >= fe->frame_shift)
            fe->prior = p_spch[fe->frame_shift - 1];
        else
            fe->prior = p_spch[len - 1];
    }
    else
        fe_short_to_frame(p_spch, p_frame, len);

    /* Zero pad up to FFT size. */
    memset(p_frame + len, 0,
           (fe->fft_size - len) * sizeof(*p_frame));


#if 0
//(CMU_SPHINX_TEST_PERFORMANCE == 1)
Iava_SysPrintf("--------fe_hamming_window start in[0] = 0x%x   in[1] = 0x%x  in[2] = 0x%x   in[3] = 0x%x",p_frame[0],p_frame[1],p_frame[2],p_frame[3]);
#endif

#if(FE_HAMMING_WINDOW_ARM_CODE)
	fe_hamming_window_debug(p_frame, fe->hamming_window, fe->frame_size,
						  fe->remove_dc);
#else
    /* Window. */
    fe_hamming_window(p_frame, fe->hamming_window, fe->frame_size,
                      fe->remove_dc);
#endif

#if 0
//(CMU_SPHINX_TEST_PERFORMANCE == 1)
	Iava_SysPrintf("--------fe_hamming_window end in[0] = 0x%x   in[1] = 0x%x  in[2] = 0x%x	in[3] = 0x%x",p_frame[0],p_frame[1],p_frame[2],p_frame[3]);
#endif


    return len;
}

int
fe_read_frame(fe_t *fe, int16 const *in, int32 len)
{
    register int i;
	register int16 *p_spch = fe->spch;

#if 0 
//(CMU_SPHINX_TEST_PERFORMANCE == 1)
	Iava_SysPrintf("--------fe_read_frame 000 len = %d!!!",len);

	short x = 0xcc3b;
	unsigned int begin,end;

	begin = Iava_SysGetUS();
	
	swap_int16_arm_code(&x);
	end = Iava_SysGetUS();
	Iava_SysPrintf("--------fe_read_frame swap_int16_arm_code = 0x%x!    time = %d us",x,end-begin);

	begin = Iava_SysGetUS();
	SWAP_INT16(&x);
	end = Iava_SysGetUS();
	Iava_SysPrintf("--------fe_read_frame SWAP_INT16 = 0x%x!    time = %d us",x,end-begin);
#endif

    if (len > fe->frame_size)
        len = fe->frame_size;

    /* Read it into the raw speech buffer. */
    memcpy(p_spch, in, len * sizeof(*in));
    /* Swap and dither if necessary. */
    if (fe->swap)
        for (i = 0; i < len; ++i)
            SWAP_INT16(&p_spch[i]);
    if (fe->dither)
        for (i = 0; i < len; ++i)
            p_spch[i] += (int16) ((!(s3_rand_int31() % 4)) ? 1 : 0);

    return fe_spch_to_frame(fe, len);
}

int
fe_shift_frame(fe_t *fe, int16 const *in, int32 len)
{
    register int offset, i;
	register int16 *p_spch = fe->spch;
	register int frame_shift = fe->frame_shift;

    if (len > frame_shift)
        len = frame_shift;
    offset = fe->frame_size - frame_shift;

    /* Shift data into the raw speech buffer. */
    memmove(p_spch, p_spch + frame_shift,
            offset * sizeof(*p_spch));
    memcpy(p_spch + offset, in, len * sizeof(*p_spch));
    /* Swap and dither if necessary. */
    if (fe->swap)
        for (i = 0; i < len; ++i)
            SWAP_INT16(&p_spch[offset + i]);
    if (fe->dither)
        for (i = 0; i < len; ++i)
            p_spch[offset + i]
                += (int16) ((!(s3_rand_int31() % 4)) ? 1 : 0);		
		//刘福良  很奇怪除法改成右移两位，反而测试速度变慢
		//
    
    return fe_spch_to_frame(fe, offset + len);
}

/**
 * Create arrays of twiddle factors.
 */
void
fe_create_twiddle(fe_t *fe)
{
    int i;

    for (i = 0; i < fe->fft_size / 4; ++i) {
        float64 a = 2 * M_PI * i / fe->fft_size;
#ifdef FIXED16
        fe->ccc[i] = (int16)(cos(a) * 0x8000);
        fe->sss[i] = (int16)(sin(a) * 0x8000);
#elif defined(FIXED_POINT)
        fe->ccc[i] = FLOAT2COS(cos(a));
        fe->sss[i] = FLOAT2COS(sin(a));
#else
        fe->ccc[i] = cos(a);
        fe->sss[i] = sin(a);
#endif
    }
}

/* Translated from the FORTRAN (obviously) from "Real-Valued Fast
 * Fourier Transform Algorithms" by Henrik V. Sorensen et al., IEEE
 * Transactions on Acoustics, Speech, and Signal Processing, vol. 35,
 * no.6.  The 16-bit version does a version of "block floating
 * point" in order to avoid rounding errors.
 */
#if defined(FIXED16)
static int
fe_fft_real(fe_t *fe)
{
    int i, j, k, m, n, lz;
    frame_t *x, xt, max;

    x = fe->frame;
    m = fe->fft_order;
    n = fe->fft_size;

    /* Bit-reverse the input. */
    j = 0;
    for (i = 0; i < n - 1; ++i) {
        if (i < j) {
            xt = x[j];
            x[j] = x[i];
            x[i] = xt;
        }
        k = n / 2;
        while (k <= j) {
            j -= k;
            k /= 2;
        }
        j += k;
    }
    /* Determine how many bits of dynamic range are in the input. */
    max = 0;
    for (i = 0; i < n; ++i)
        if (abs(x[i]) > max)
            max = abs(x[i]);
    /* The FFT has a gain of M bits, so we need to attenuate the input
     * by M bits minus the number of leading zeroes in the input's
     * range in order to avoid overflows.  */
    for (lz = 0; lz < m; ++lz)
        if (max & (1 << (15-lz)))
            break;

    /* Basic butterflies (2-point FFT, real twiddle factors):
     * x[i]   = x[i] +  1 * x[i+1]
     * x[i+1] = x[i] + -1 * x[i+1]
     */
    /* The quantization error introduced by attenuating the input at
     * any given stage of the FFT has a cascading effect, so we hold
     * off on it until it's absolutely necessary. */
    for (i = 0; i < n; i += 2) {
        int atten = (lz == 0);
        xt = x[i] >> atten;
        x[i]     = xt + (x[i + 1] >> atten);
        x[i + 1] = xt - (x[i + 1] >> atten);
    }

    /* The rest of the butterflies, in stages from 1..m */
    for (k = 1; k < m; ++k) {
        int n1, n2, n4;
        /* Start attenuating once we hit the number of leading zeros. */
        int atten = (k >= lz);

        n4 = k - 1;
        n2 = k;
        n1 = k + 1;
        /* Stride over each (1 << (k+1)) points */
        for (i = 0; i < n; i += (1 << n1)) {
            /* Basic butterfly with real twiddle factors:
             * x[i]          = x[i] +  1 * x[i + (1<<k)]
             * x[i + (1<<k)] = x[i] + -1 * x[i + (1<<k)]
             */
            xt = x[i] >> atten;
            x[i]             = xt + (x[i + (1 << n2)] >> atten);
            x[i + (1 << n2)] = xt - (x[i + (1 << n2)] >> atten);

            /* The other ones with real twiddle factors:
             * x[i + (1<<k) + (1<<(k-1))]
             *   = 0 * x[i + (1<<k-1)] + -1 * x[i + (1<<k) + (1<<k-1)]
             * x[i + (1<<(k-1))]
             *   = 1 * x[i + (1<<k-1)] +  0 * x[i + (1<<k) + (1<<k-1)]
             */
            x[i + (1 << n2) + (1 << n4)] = -x[i + (1 << n2) + (1 << n4)] >> atten;
            x[i + (1 << n4)]             =  x[i + (1 << n4)] >> atten;
            
            /* Butterflies with complex twiddle factors.
             * There are (1<<k-1) of them.
             */
            for (j = 1; j < (1 << n4); ++j) {
                frame_t cc, ss, t1, t2;
                int i1, i2, i3, i4;

                i1 = i + j;
                i2 = i + (1 << n2) - j;
                i3 = i + (1 << n2) + j;
                i4 = i + (1 << n2) + (1 << n2) - j;

                /*
                 * cc = real(W[j * n / (1<<(k+1))])
                 * ss = imag(W[j * n / (1<<(k+1))])
                 */
                cc = fe->ccc[j << (m - n1)];
                ss = fe->sss[j << (m - n1)];

                /* There are some symmetry properties which allow us
                 * to get away with only four multiplications here. */
                {
                    int32 tmp1, tmp2;
                    tmp1 = (int32)x[i3] * cc + (int32)x[i4] * ss;
                    tmp2 = (int32)x[i3] * ss - (int32)x[i4] * cc;
                    t1 = (int16)(tmp1 >> 15) >> atten;
                    t2 = (int16)(tmp2 >> 15) >> atten;
                }

                x[i4] = (x[i2] >> atten) - t2;
                x[i3] = (-x[i2] >> atten) - t2;
                x[i2] = (x[i1] >> atten) - t1;
                x[i1] = (x[i1] >> atten) + t1;
            }
        }
    }

    /* Return the residual scaling factor. */
    return lz;
}



#else /* !FIXED16 */


#if 0

static int
fe_fft_real(fe_t *fe)
{
    int i, j, k, m, n;
    frame_t *x, xt;


#if(FE_FFT_REAL_ARM_CODE)

#if 0
//(CMU_SPHINX_TEST_PERFORMANCE == 1)
//	Iava_SysPrintf("--------fe_fft_real_arm_code  fe->frame = 0x%x!!!",fe->frame);
	Iava_SysPrintf("--------fe_fft_real_arm_code  fe->fft_order = 0x%x!!!",fe->fft_order);
	Iava_SysPrintf("--------fe_fft_real_arm_code  fe->fft_size = 0x%x!!!",fe->fft_size);

#endif	
	m = fe_fft_real_arm_code(fe);
#else

	x = fe->frame;
    m = fe->fft_order;
    n = fe->fft_size;
	
    /* Bit-reverse the input. */
    j = 0;
    for (i = 0; i < n - 1; ++i) {
        if (i < j) {
            xt = x[j];
            x[j] = x[i];
            x[i] = xt;
        }
        //k = n / 2;
		k = n >> 1;
        while (k <= j) {
            j -= k;
            //k /= 2;
            k = k >> 1;
        }
        j += k;
    }

    /* Basic butterflies (2-point FFT, real twiddle factors):
     * x[i]   = x[i] +  1 * x[i+1]
     * x[i+1] = x[i] + -1 * x[i+1]
     */
    for (i = 0; i < n; i += 2) {
        xt = x[i];
        x[i]     = (xt + x[i + 1]);
        x[i + 1] = (xt - x[i + 1]);
    }


    /* The rest of the butterflies, in stages from 1..m */
    for (k = 1; k < m; ++k) {
        int n1, n2, n4;

        n4 = k - 1;
        n2 = k;
        n1 = k + 1;

		fft_order_shift_table[32] = k;
		fft_order_shift_table[43] = m - n1;
		fft_order_shift_table[33] = n1;
		fft_order_shift_table[34] = n2;
		fft_order_shift_table[35] = n4;
		fft_order_shift_table[36] = 1<<n1;
		fft_order_shift_table[37] = 1<<n2;
		fft_order_shift_table[38] = 1<<n4;
		
		Iava_SysPrintf("ASM DEBUG = a0");
		Iava_SysPrintf("DEBUG = %x", k);
		Iava_SysPrintf("DEBUG = %x", m);
		Iava_SysPrintf("DEBUG = %x", n1);		
		Iava_SysPrintf("DEBUG = %x", (m - n1));

		
        /* Stride over each (1 << (k+1)) points */
        for (i = 0; i < n; i += fft_order_shift_table[36]) 
		{
			int index = 0;
			int index_1 = 0;

			index = i + fft_order_shift_table[37];
			index_1 = index + fft_order_shift_table[38];
            /* Basic butterfly with real twiddle factors:
             * x[i]          = x[i] +  1 * x[i + (1<<k)]
             * x[i + (1<<k)] = x[i] + -1 * x[i + (1<<k)]
             */
            xt = x[i];
            x[i]            = (xt + x[index]);
            x[index] 		= (xt - x[index]);

            /* The other ones with real twiddle factors:
             * x[i + (1<<k) + (1<<(k-1))]
             *   = 0 * x[i + (1<<k-1)] + -1 * x[i + (1<<k) + (1<<k-1)]
             * x[i + (1<<(k-1))]
             *   = 1 * x[i + (1<<k-1)] +  0 * x[i + (1<<k) + (1<<k-1)]
             */
            x[index_1] = -x[index_1];
            //x[i + (1 << n4)]             =  x[i + (1 << n4)];

/*
			Iava_SysPrintf("ASM DEBUG = a2");
			Iava_SysPrintf("DEBUG = %x", xt);
			Iava_SysPrintf("DEBUG = %x", x[i]);
			Iava_SysPrintf("DEBUG = %x", x[i + (1 << n2)]);
			Iava_SysPrintf("DEBUG = %x", x[i + (1 << n2) + (1 << n4)]);
			
*/
            /* Butterflies with complex twiddle factors.
             * There are (1<<k-1) of them.
             */


			#if 1
			
				fe_fft_real_arm_code_test(fe,i);
			#else
            for (j = 1; j < fft_order_shift_table[38]; ++j) {
                frame_t cc, ss, t1, t2;
                int i1, i2, i3, i4;
				int temp = j << fft_order_shift_table[43];

                i1 = i + j;
                i2 = index - j;
                i3 = index + j;
                //i4 = i + (1 << n2) + (1 << n2) - j;
				i4 = i + (1 << n1) - j;

                /*
                 * cc = real(W[j * n / (1<<(k+1))])
                 * ss = imag(W[j * n / (1<<(k+1))])
                 */
                cc = fe->ccc[temp];
                ss = fe->sss[temp];


				Iava_SysPrintf("ASM DEBUG = a6");
				Iava_SysPrintf("DEBUG = %x", fft_order_shift_table[43]);
				Iava_SysPrintf("DEBUG = %x", temp);
				Iava_SysPrintf("DEBUG = %x", cc);
				Iava_SysPrintf("DEBUG = %x", ss);


				Iava_SysPrintf("ASM DEBUG = a9");
				Iava_SysPrintf("DEBUG = %x", x[i3]);
				Iava_SysPrintf("DEBUG = %x", x[i4]);
                /* There are some symmetry properties which allow us
                 * to get away with only four multiplications here. */
                t1 = COSMUL(x[i3], cc) + COSMUL(x[i4], ss);
                t2 = COSMUL(x[i3], ss) - COSMUL(x[i4], cc);

				Iava_SysPrintf("ASM DEBUG = ab");
				Iava_SysPrintf("DEBUG = %x",t1);
				Iava_SysPrintf("DEBUG = %x",t2);

				
                x[i4] = (x[i2] - t2);
                x[i3] = (-x[i2] - t2);
				
				Iava_SysPrintf("ASM DEBUG = ac");
				Iava_SysPrintf("DEBUG = %x",i4);
				Iava_SysPrintf("DEBUG = %x",x[i4]);
				Iava_SysPrintf("DEBUG = %x",i3);
				Iava_SysPrintf("DEBUG = %x",x[i3]);
				
                x[i2] = (x[i1] - t1);
                x[i1] = (x[i1] + t1);

				Iava_SysPrintf("ASM DEBUG = ad");
				Iava_SysPrintf("DEBUG = %x",i2);
				Iava_SysPrintf("DEBUG = %x",x[i2]);
				Iava_SysPrintf("DEBUG = %x",i1);
				Iava_SysPrintf("DEBUG = %x",x[i1]);
            }
		#endif
        }
    }
#endif

    /* This isn't used, but return it for completeness. */
    return m;
}

#endif


#if 1
static int
fe_fft_real(fe_t *fe)
{
    int i, j, k, m, n;
    frame_t *x, xt;


	x = fe->frame;
    m = fe->fft_order;
    n = fe->fft_size;
	
    /* Bit-reverse the input. */
    j = 0;
    for (i = 0; i < n - 1; ++i) {
        if (i < j) {
            xt = x[j];
            x[j] = x[i];
            x[i] = xt;
        }
        //k = n / 2;
		k = n >> 1;
        while (k <= j) {
            j -= k;
            //k /= 2;
            k = k >> 1;
        }
        j += k;
    }

    /* Basic butterflies (2-point FFT, real twiddle factors):
     * x[i]   = x[i] +  1 * x[i+1]
     * x[i+1] = x[i] + -1 * x[i+1]
     */
    for (i = 0; i < n; i += 2) {
        xt = x[i];
        x[i]     = (xt + x[i + 1]);
        x[i + 1] = (xt - x[i + 1]);
    }


    /* The rest of the butterflies, in stages from 1..m */
    for (k = 1; k < m; ++k) {
        int n1, n2, n4;

        n4 = k - 1;
        n2 = k;
        n1 = k + 1;


		
        /* Stride over each (1 << (k+1)) points */
        for (i = 0; i < n; i += (1 << n1)) {
            /* Basic butterfly with real twiddle factors:
             * x[i]          = x[i] +  1 * x[i + (1<<k)]
             * x[i + (1<<k)] = x[i] + -1 * x[i + (1<<k)]
             */
            xt = x[i];
            x[i]             = (xt + x[i + (1 << n2)]);
            x[i + (1 << n2)] = (xt - x[i + (1 << n2)]);

            /* The other ones with real twiddle factors:
             * x[i + (1<<k) + (1<<(k-1))]
             *   = 0 * x[i + (1<<k-1)] + -1 * x[i + (1<<k) + (1<<k-1)]
             * x[i + (1<<(k-1))]
             *   = 1 * x[i + (1<<k-1)] +  0 * x[i + (1<<k) + (1<<k-1)]
             */
            x[i + (1 << n2) + (1 << n4)] = -x[i + (1 << n2) + (1 << n4)];
            x[i + (1 << n4)]             =  x[i + (1 << n4)];


            /* Butterflies with complex twiddle factors.
             * There are (1<<k-1) of them.
             */


            for (j = 1; j < (1 << n4); ++j) {
                frame_t cc, ss, t1, t2;
                int i1, i2, i3, i4;

                i1 = i + j;
                i2 = i + (1 << n2) - j;
                i3 = i + (1 << n2) + j;
                i4 = i + (1 << n2) + (1 << n2) - j;
				//i4 = i + (1 << n1) - j;

                /*
                 * cc = real(W[j * n / (1<<(k+1))])
                 * ss = imag(W[j * n / (1<<(k+1))])
                 */
                cc = fe->ccc[j << (m - n1)];
                ss = fe->sss[j << (m - n1)];


		
                /* There are some symmetry properties which allow us
                 * to get away with only four multiplications here. */
                t1 = COSMUL(x[i3], cc) + COSMUL(x[i4], ss);
                t2 = COSMUL(x[i3], ss) - COSMUL(x[i4], cc);

                x[i4] = (x[i2] - t2);
                x[i3] = (-x[i2] - t2);
                x[i2] = (x[i1] - t1);
                x[i1] = (x[i1] + t1);
            }

        }
    }


    /* This isn't used, but return it for completeness. */
    return m;
}

#endif

#endif /* !FIXED16 */

static void
fe_spec_magnitude(fe_t *fe)
{
    register frame_t *fft;
    powspec_t *spec;
    int32 j, scale, fftsize,fftsize_x;

    /* Do FFT and get the scaling factor back (only actually used in
     * fixed-point).  Note the scaling factor is expressed in bits. */

#if(FE_FFT_REAL_ARM_CODE)
	scale = fe_fft_real_arm_code(fe);
#else
    scale = fe_fft_real(fe);
#endif

    /* Convenience pointers to make things less awkward below. */
    fft = fe->frame;
    spec = fe->spec;
    fftsize = fe->fft_size;

    /* We need to scale things up the rest of the way to N. */
    scale = fe->fft_order - scale;

    /* The first point (DC coefficient) has no imaginary part */
    {
#ifdef FIXED16
        spec[0] = fixlog(abs(fft[0]) << scale) * 2;
#elif defined(FIXED_POINT)
        spec[0] = FIXLN(abs(fft[0]) << scale) * 2;
#else
        spec[0] = fft[0] * fft[0];
#endif
    }

	fftsize_x = fftsize / 2;
    for (j = 1; j <= fftsize_x; j++) {
#ifdef FIXED16
        int32 rr = fixlog(abs(fft[j]) << scale) * 2;
        int32 ii = fixlog(abs(fft[fftsize - j]) << scale) * 2;
        spec[j] = fe_log_add(rr, ii);
		//spec[j] = rr + ii;
#elif defined(FIXED_POINT)
        int32 rr = FIXLN(abs(fft[j]) << scale) * 2;
        int32 ii = FIXLN(abs(fft[fftsize - j]) << scale) * 2;
        spec[j] = fe_log_add(rr, ii);
#else
        spec[j] = fft[j] * fft[j] + fft[fftsize - j] * fft[fftsize - j];
#endif
    }
}

static void
fe_mel_spec(fe_t * fe)
{


#if 0
	int whichfilt;
    register powspec_t *spec, *mfspec;
	melfb_t * p_mel_fb;
	int num_filters;
	int16 *p_spec_start,*p_filt_start,*p_filt_width;
	register mfcc_t *filt_coeffs;

    /* Convenience poitners. */
    spec = fe->spec;
    mfspec = fe->mfspec;
	p_mel_fb = fe->mel_fb;
	num_filters = p_mel_fb->num_filters;
	p_spec_start = p_mel_fb->spec_start;
	p_filt_start = p_mel_fb->filt_start;
	filt_coeffs = p_mel_fb->filt_coeffs;
	p_filt_width = p_mel_fb->filt_width;

	
    for (whichfilt = 0; whichfilt < num_filters; whichfilt++) {
        int spec_start, filt_start, i,x;

        spec_start = p_spec_start[whichfilt];
        filt_start = p_filt_start[whichfilt];

        mfspec[whichfilt] = spec[spec_start] + filt_coeffs[filt_start];

		x = p_filt_width[whichfilt];
        for (i = 1; i < x; i++) 
		{
            mfspec[whichfilt] = fe_log_add(mfspec[whichfilt],
                                           spec[spec_start + i] +
                                           filt_coeffs[filt_start + i]);
        }
    }

#else
    int whichfilt;
    powspec_t *spec, *mfspec;
	int num_filters = fe->mel_fb->num_filters;

    /* Convenience poitners. */
    spec = fe->spec;
    mfspec = fe->mfspec;

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	Iava_SysPrintf("--------fe_mel_spec fe->mel_fb->num_filters== %d",num_filters);
#endif	

    for (whichfilt = 0; whichfilt < num_filters; whichfilt++) {
        int spec_start, filt_start, i;

        spec_start = fe->mel_fb->spec_start[whichfilt];
        filt_start = fe->mel_fb->filt_start[whichfilt];

#ifdef FIXED_POINT
        mfspec[whichfilt] = spec[spec_start] + fe->mel_fb->filt_coeffs[filt_start];
        for (i = 1; i < fe->mel_fb->filt_width[whichfilt]; i++) {
            mfspec[whichfilt] = fe_log_add(mfspec[whichfilt],
                                           spec[spec_start + i] +
                                           fe->mel_fb->filt_coeffs[filt_start + i]);
        }
#else                           /* !FIXED_POINT */
        mfspec[whichfilt] = 0;
        for (i = 0; i < fe->mel_fb->filt_width[whichfilt]; i++)
            mfspec[whichfilt] +=
                spec[spec_start + i] * fe->mel_fb->filt_coeffs[filt_start + i];
#endif                          /* !FIXED_POINT */
    }

#endif
}

static void
fe_mel_cep(fe_t * fe, mfcc_t *mfcep)
{
    int32 i;
    powspec_t *mfspec;

    /* Convenience pointer. */
    mfspec = fe->mfspec;

#ifndef FIXED_POINT
    for (i = 0; i < fe->mel_fb->num_filters; ++i) 
	{
 /* It's already in log domain for fixed point */
        if (mfspec[i] > 0)
            mfspec[i] = log(mfspec[i]);
        else                    /* This number should be smaller than anything
                                 * else, but not too small, so as to avoid
                                 * infinities in the inverse transform (this is
                                 * the frequency-domain equivalent of
                                 * dithering) */
            mfspec[i] = -10.0;
                          /* !FIXED_POINT */
    }
#endif


    /* If we are doing LOG_SPEC, then do nothing. */
    if (fe->log_spec == RAW_LOG_SPEC) 
	{
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
//	Iava_SysPrintf("--------fe->log_spec == RAW_LOG_SPEC!!!");
#endif	
        for (i = 0; i < fe->feature_dimension; i++) {
            mfcep[i] = (mfcc_t) mfspec[i];
        }
    }
    /* For smoothed spectrum, do DCT-II followed by (its inverse) DCT-III */
    else if (fe->log_spec == SMOOTH_LOG_SPEC) 
	{
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
//	Iava_SysPrintf("--------fe->log_spec == SMOOTH_LOG_SPEC!!!");
#endif	

        /* FIXME: This is probably broken for fixed-point. */
        fe_dct2(fe, mfspec, mfcep, 0);
        fe_dct3(fe, mfcep, mfspec);
        for (i = 0; i < fe->feature_dimension; i++) {
            mfcep[i] = (mfcc_t) mfspec[i];
        }
    }
    else if (fe->transform == DCT_II)
    {
    
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	//经测试，一般都是执行到这里
	//Iava_SysPrintf("--------fe->transform == DCT_II!!!");
#endif	
        fe_dct2(fe, mfspec, mfcep, FALSE);
    }
    else if (fe->transform == DCT_HTK)
    {
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
//		Iava_SysPrintf("--------fe->transform == DCT_HTK!!!");
#endif	
        fe_dct2(fe, mfspec, mfcep, TRUE);
    }
    else
    {
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
//				Iava_SysPrintf("--------fe->transform !!!");
#endif

        fe_spec2cep(fe, mfspec, mfcep);
    }

    return;
}

void
fe_spec2cep(fe_t * fe, const powspec_t * mflogspec, mfcc_t * mfcep)
{
    int32 i, j, beta;

    /* Compute C0 separately (its basis vector is 1) to avoid
     * costly multiplications. */
    mfcep[0] = mflogspec[0] / 2; /* beta = 0.5 */
    for (j = 1; j < fe->mel_fb->num_filters; j++)
	mfcep[0] += mflogspec[j]; /* beta = 1.0 */
    mfcep[0] /= (frame_t) fe->mel_fb->num_filters;

    for (i = 1; i < fe->num_cepstra; ++i) {
        mfcep[i] = 0;
        for (j = 0; j < fe->mel_fb->num_filters; j++) {
            if (j == 0)
                beta = 1;       /* 0.5 */
            else
                beta = 2;       /* 1.0 */
            mfcep[i] += COSMUL(mflogspec[j],
                               fe->mel_fb->mel_cosine[i][j]) * beta;
        }
	/* Note that this actually normalizes by num_filters, like the
	 * original Sphinx front-end, due to the doubled 'beta' factor
	 * above.  */
        mfcep[i] /= (frame_t) fe->mel_fb->num_filters * 2;
    }
}

void
fe_dct2(fe_t * fe, const powspec_t * mflogspec, mfcc_t * mfcep, int htk)
{

#if(FE_DCT2_ARM_CODE == 1)
	//int temp[10] = {0,1,2,3,4,5,6,7,8,9};
	//int sum = get_sum_arm_code(temp,10);
	//Iava_SysPrintf("--------sum == %d",sum);
	
	fe_dct2_arm_code(fe,mflogspec,mfcep,htk);
#else
#if 1
	
	int32 i, j,num_filters;
	melfb_t *p_mel_fb = fe->mel_fb;
	uint8 num_cepstra = fe->num_cepstra;
	mfcc_t *p_temp,*p_temp1;
	mfcc_t sqrt_inv_2n;

	num_filters = p_mel_fb->num_filters;

    /* Compute C0 separately (its basis vector is 1) to avoid
     * costly multiplications. */
    mfcep[0] = mflogspec[0];
    for (j = 1; j < num_filters; j++)
		mfcep[0] += mflogspec[j];


	
    if (htk)
        mfcep[0] = COSMUL(mfcep[0], p_mel_fb->sqrt_inv_2n);
    else /* sqrt(1/N) = sqrt(2/N) * 1/sqrt(2) */
        mfcep[0] = COSMUL(mfcep[0], p_mel_fb->sqrt_inv_n);


	num_cepstra = fe->num_cepstra;
	p_temp1 = p_mel_fb->mel_cosine;
	sqrt_inv_2n = p_mel_fb->sqrt_inv_2n;
    for (i = 1; i < num_cepstra; ++i) 
	{
        mfcep[i] = 0;
		p_temp = p_temp1[i];
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
		//Iava_SysPrintf("--------p_temp1[%d] == 0x%x",i,p_temp);
#endif	
        for (j = 0; j < num_filters; j++) 
		{
	    	mfcep[i] += COSMUL(mflogspec[j],p_temp[j]);
        }
		
        mfcep[i] = COSMUL(mfcep[i], sqrt_inv_2n);
    }
#else
    int32 i, j;

    /* Compute C0 separately (its basis vector is 1) to avoid
     * costly multiplications. */
    mfcep[0] = mflogspec[0];
    for (j = 1; j < fe->mel_fb->num_filters; j++)
		mfcep[0] += mflogspec[j];


	
    if (htk)
        mfcep[0] = COSMUL(mfcep[0], fe->mel_fb->sqrt_inv_2n);
    else /* sqrt(1/N) = sqrt(2/N) * 1/sqrt(2) */
        mfcep[0] = COSMUL(mfcep[0], fe->mel_fb->sqrt_inv_n);



    for (i = 1; i < fe->num_cepstra; ++i) 
	{
        mfcep[i] = 0;
        for (j = 0; j < fe->mel_fb->num_filters; j++) 
		{
	    	mfcep[i] += COSMUL(mflogspec[j],fe->mel_fb->mel_cosine[i][j]);
        }
		
        mfcep[i] = COSMUL(mfcep[i], fe->mel_fb->sqrt_inv_2n);
    }

#endif

#endif

}

void
fe_lifter(fe_t *fe, mfcc_t *mfcep)
{
    int32 i;

    if (fe->mel_fb->lifter_val == 0)
    {
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
		//JAVA层的设置，直接返回，暂时不用优化
		//Iava_SysPrintf("--------fe->mel_fb->lifter_val == 0!!!");
#endif	
        return;
    }

    for (i = 0; i < fe->num_cepstra; ++i) {
        mfcep[i] = MFCCMUL(mfcep[i], fe->mel_fb->lifter[i]);
    }
}

void
fe_dct3(fe_t * fe, const mfcc_t * mfcep, powspec_t * mflogspec)
{
    int32 i, j;

    for (i = 0; i < fe->mel_fb->num_filters; ++i) {
        mflogspec[i] = COSMUL(mfcep[0], SQRT_HALF);
        for (j = 1; j < fe->num_cepstra; j++) {
            mflogspec[i] += COSMUL(mfcep[j],
                                    fe->mel_fb->mel_cosine[j][i]);
        }
        mflogspec[i] = COSMUL(mflogspec[i], fe->mel_fb->sqrt_inv_2n);
    }
}

int32
fe_write_frame(fe_t * fe, mfcc_t * fea)
{
    fe_spec_magnitude(fe);
#if (FE_MEL_SPEC_ARM_CODE == 1)
	fe_mel_spec_arm_code(fe);
#else
    fe_mel_spec(fe);
#endif

    fe_mel_cep(fe, fea);

    fe_lifter(fe, fea);

    return 1;
}

void *
fe_create_2d(int32 d1, int32 d2, int32 elem_size)
{
    return (void *)ckd_calloc_2d(d1, d2, elem_size);
}

void
fe_free_2d(void *arr)
{
    ckd_free_2d((void **)arr);
}
