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

#ifndef __FE_INTERNAL_H__
#define __FE_INTERNAL_H__

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include "sphinxbase/fe.h"
#include "sphinxbase/fixpoint.h"

#ifdef __cplusplus
extern "C" {
#endif
#if 0
/* Fool Emacs. */
}
#endif

#ifdef FIXED16
/* Q15 format */
typedef int16 frame_t;
typedef int16 window_t;
typedef int32 powspec_t;
typedef struct { int16 r, i; } complex;
#elif defined(FIXED_POINT)
typedef fixed32 frame_t;
typedef int32 powspec_t;
typedef fixed32 window_t;
typedef struct { fixed32 r, i; } complex;
#else /* FIXED_POINT */
typedef float64 frame_t;
typedef float64 powspec_t;
typedef float64 window_t;
typedef struct { float64 r, i; } complex;
#endif /* FIXED_POINT */

/* Values for the 'logspec' field. */
enum {
	RAW_LOG_SPEC = 1,
	SMOOTH_LOG_SPEC = 2
};

/* Values for the 'transform' field. */
enum {
	LEGACY_DCT = 0,
	DCT_II = 1,
        DCT_HTK = 2
};

typedef struct melfb_s melfb_t;
/** Base Struct to hold all structure for MFCC computation. */
struct melfb_s {
    float32 sampling_rate;				// [r0,#0x00]
    int32 num_cepstra;					// [r0,#0x04]
    int32 num_filters;					// [r0,#0x08]
    int32 fft_size;					// [r0,#0x0c]
    float32 lower_filt_freq;			// [r0,#0x10]
    float32 upper_filt_freq;			// [r0,#0x14]
    /* DCT coefficients. */
    mfcc_t **mel_cosine;				// [r0,#0x18]
    /* Filter coefficients. */
    mfcc_t *filt_coeffs;				// [r0,#0x1c]
    int16 *spec_start;					// [r0,#0x20]
    int16 *filt_start;					// [r0,#0x24]
    int16 *filt_width;					// [r0,#0x28]
    /* Luxury mobile home. */
    int32 doublewide;					// [r0,#0x2c]
    char const *warp_type;				// [r0,#0x30]
    char const *warp_params;			// [r0,#0x34]
    uint32 warp_id;						// [r0,#0x38]
    /* Precomputed normalization constants for unitary DCT-II/DCT-III */
    mfcc_t sqrt_inv_n, sqrt_inv_2n;	// [r0,#0x3c]	// [r0,#0x40]
    /* Value and coefficients for HTK-style liftering */
    int32 lifter_val;					// [r0,#0x44]
    mfcc_t *lifter;						// [r0,#0x48]
    /* Normalize filters to unit area */
    int32 unit_area;					// [r0,#0x4c]
    /* Round filter frequencies to DFT points (hurts accuracy, but is
       useful for legacy purposes) */
    int32 round_filters;				// [r0,#0x50]
};

/* sqrt(1/2), also used for unitary DCT-II/DCT-III */
#define SQRT_HALF FLOAT2MFCC(0.707106781186548)

/** Structure for the front-end computation. */
struct fe_s {
    cmd_ln_t *config;				// [r0,#0x00]
    int refcount;					// [r0,#0x04]

    float32 sampling_rate;			// [r0,#0x08]
    int16 frame_rate;				// [r0,#0x0C]
    int16 frame_shift;				// [r0,#0x0E]

    float32 window_length;			// [r0,#0x10]
    int16 frame_size;				// [r0,#0x14]
    int16 fft_size;				// [r0,#0x16]

    uint8 fft_order;				// [r0,#0x18]
    uint8 feature_dimension;		// [r0,#0x19]
    uint8 num_cepstra;				// [r0,#0x1A]
    uint8 remove_dc;				// [r0,#0x1B]
    uint8 log_spec;				// [r0,#0x1C]
    uint8 swap;						// [r0,#0x1D]
    uint8 dither;					// [r0,#0x1E]
    uint8 transform;				// [r0,#0x1F]

    float32 pre_emphasis_alpha;	// [r0,#0x20]
    int32 seed;						// [r0,#0x24]

    int16 frame_counter;			// [r0,#0x28]
    uint8 start_flag;				// [r0,#0x2A]
    uint8 reserved;				// [r0,#0x2B]

    /* Twiddle factors for FFT. */
    frame_t *ccc, *sss;				// [r0,#0x2C]  // [r0,#0x30]
    /* Mel filter parameters. */
    melfb_t *mel_fb;				// [r0,#0x34]
    /* Half of a Hamming Window. */
    window_t *hamming_window;		// [r0,#0x38]

    /* Temporary buffers for processing. */
    /* FIXME: too many of these. */
    int16 *spch;					// [r0,#0x3C]
    frame_t *frame;					// [r0,#0x40]
    powspec_t *spec, *mfspec;		// [r0,#0x44]  // [r0,#0x48]
    int16 *overflow_samps;			// [r0,#0x4C]
    int16 num_overflow_samps;    	// [r0,#0x50]
    int16 prior;					// [r0,#0x52]
};

#define BB_SAMPLING_RATE 16000
#define DEFAULT_BB_FFT_SIZE 512
#define DEFAULT_BB_FRAME_SHIFT 160
#define DEFAULT_BB_NUM_FILTERS 40
#define DEFAULT_BB_LOWER_FILT_FREQ 133.33334
#define DEFAULT_BB_UPPER_FILT_FREQ 6855.4976

#define NB_SAMPLING_RATE 8000
#define DEFAULT_NB_FFT_SIZE 256
#define DEFAULT_NB_FRAME_SHIFT 80
#define DEFAULT_NB_NUM_FILTERS 31
#define DEFAULT_NB_LOWER_FILT_FREQ 200
#define DEFAULT_NB_UPPER_FILT_FREQ 3500

void fe_init_dither(int32 seed);

/* Apply 1/2 bit noise to a buffer of audio. */
int32 fe_dither(int16 *buffer, int32 nsamps);

/* Load a frame of data into the fe. */
int fe_read_frame(fe_t *fe, int16 const *in, int32 len);

/* Shift the input buffer back and read more data. */
int fe_shift_frame(fe_t *fe, int16 const *in, int32 len);

/* Process a frame of data into features. */
int32 fe_write_frame(fe_t *fe, mfcc_t *fea);

/* Initialization functions. */
int32 fe_build_melfilters(melfb_t *MEL_FB);
int32 fe_compute_melcosine(melfb_t *MEL_FB);
void fe_create_hamming(window_t *in, int32 in_len);
void fe_create_twiddle(fe_t *fe);

/* Miscellaneous processing functions. */
void fe_spec2cep(fe_t * fe, const powspec_t * mflogspec, mfcc_t * mfcep);
void fe_dct2(fe_t *fe, const powspec_t *mflogspec, mfcc_t *mfcep, int htk);
void fe_dct3(fe_t *fe, const mfcc_t *mfcep, powspec_t *mflogspec);

#ifdef __cplusplus
}
#endif

#endif /* __FE_INTERNAL_H__ */
