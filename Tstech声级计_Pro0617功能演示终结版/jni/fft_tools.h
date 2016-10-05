#pragma once
#ifdef TOOLS_EXPORTS
#define  TOOLS_API
#else
#define  TOOLS_API
#endif

#include <complex>
#include <algorithm>
#include <stdint.h>
#define _USE_MATH_DEFINES
#include <math.h>
#include "Common.h"

double TOOLS_API getKaiserCoeff(double stop_db, double *D);
double TOOLS_API kaiser(double index, double winlen, double alpha, double bessel_alpha = 0);
double TOOLS_API kaiser_(double index, double winlen, double alpha = 6.5, double bessel_alpha = 0);//alpha=6.5-> Sperrdämpfung etwa 70 dB

// Implementation of the Fast Fourier Transform	

////////////////////////////////////////////////////////////////
// Basic FFT subroutines
////////////////////////////////////////////////////////////////
#define two(x)		(1 << (x))
//! bit reverse of a B-bit integer n
inline int bitrev(int n, int B)
{
	int m, r;

	for (r = 0, m = B - 1; m >= 0; m--)
	{
		if ((n >> m) == 1)		// if 2^m term is present, then
		{
			r = r + two(B - 1 - m);	// add 2^(B-1-m) to r, and
			n = n - two(m);			// substract 2^m from n
		}
	}
	return r;
}

//! bit reversal routine. N must be a power of two
template<typename T>
inline void shuffle(uint32_t N, std::complex<T> *X)
{
	unsigned long n, r, B = 1;	// B is number of bits
#ifndef _MSC_VER
	while ((N >> B) > 0)
		B++;
	B--;	// N=2^B
#else
	_BitScanForward(&B, N);
	--B;
	r = 0;
#endif

	for (n = 1; n < N - 1; n++)
	{
#ifdef _MSC_VER
		int index = B;
		while (_bittestandcomplement((LONG*)&r, index--));
#else
		r = bitrev(n, B);			// bit reversed version of n
#endif
		if (r < n) 
			continue;	// swap only half of the n's.	
		std::swap(*(X + n), *(X + r));	// swap by addresses
	}
}



//! DFT merging for radix 2 decimation-in-time FFT
template<typename T>
inline void dftmerge(int N, std::complex<T> *XF)
{
	int k, i, p, q, M;
	std::complex<T> A, B, V, W;

	M = 2;
	while (M <= N)						// two(M/2)-DFT's into one M-DFT
	{
		W = exp(std::complex<T>(0, -2 * M_PI / M));	// order-M twiddle factor
		V = std::complex<T>(1, 0);					// succesive powers of W
		for (k = 0; k < M / 2; k++)			// index for a (M/2)-DFT
		{
			for (i = 0; i < N; i += M)		// ith butterfly; increment by M
			{
				p = k + i;				// absolute indices for
				q = p + M / 2;				// ith butterfly
				A = XF[p];
				B = XF[q] * V;	// V=W^k
				XF[p] = A + B;	// butterfly operations	
				XF[q] = A - B;
			}
			V = V * W;			// V=W*V=W^(k+1)
		}
		M = 2 * M;						// next stage
	}
}

template<typename T>
inline void fft(int N, std::complex<T> *X)
{
	shuffle(N, X);
	dftmerge(N, X);
}

/*! Power Spectrum of two real signals for positive frequencies
* The first value are according to 0 Hz.
*/
template<typename T>
inline void doublefft(int N, std::complex<T> *X, double *data)
{
	int k, n1 = 0, n2 = N / 2 + 1;
	std::complex<T> xcon;

	fft(N, X);	// N-FFT for two real signals

	data[n1] = X[0].real() * X[0].real();
	data[n2] = X[0].imag() * X[0].imag();

	for (k = 1; k < n2; k++)
	{	// dc is first value
		xcon = conj(X[N - k]);
		data[n1 + k] = norm(X[k] + xcon) / 2.0;// power
		data[n2 + k] = norm(X[k] - xcon) / 2.0;
	}
}

inline void multiplyArray(double *array, unsigned long channels, double factor)
{
	for (unsigned long c = 0; c < channels; c++)
		array[c] *= factor;
}

double TOOLS_API getWindow(double *window, uint32_t winlen, WindowType type, double winpar = 0);
void TOOLS_API getFFTScale(double *window, uint32_t winlen, uint32_t fftlen, double& scale_magnitude, double& scale_power);
