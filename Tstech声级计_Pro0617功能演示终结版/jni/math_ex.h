#pragma once
#ifdef TOOLS_EXPORTS
#define  TOOLS_API
#else
#define  TOOLS_API
#endif

#include <math.h>
#include <complex>

#ifdef _MSC_VER
#if _MSC_VER < 1700
#include <emmintrin.h>
#include <limits.h>
inline double round(double x)
{
#ifdef _WIN64
	return _mm_cvtsd_si64x(_mm_load_sd(&x));
#else
	_asm
	{
		fld x
		frndint
		fstp x
	}
	return x;
#endif
}

#endif
#endif

TOOLS_API double round(double data, unsigned char stellen);

template<class T>
T asinh(T x)
{
	return (log(x + sqrt(x*x + 1)));
}

template<class T>
T acosh(T x)
{
	T y = 0;
	if (x > 1.0)
		y = log(x + sqrt(x*x - 1));
	return y;
}

inline double bessel(double x)
{
	double eps = 1e-9, S = 1, D = 1, T;
	unsigned long n = 1;

	while (D > eps*S)
	{
		T = x / (2.0 * n++);
		D *= (T*T);
		S += D;
	}

	return S;
}

inline double angle(double real, double imag)
{
	return (-atan2(imag, real));
}

/*! compute integer exponent such as 2 elavated to this power
* is larger or equal than x parameter.
*
*  2^nextpow2(x) >= x
*/
inline int nextpow2(double x)
{
	if (x <= 0)
		return 0;

	// basis 2 logarithm
	return ((int)ceil(log(x) / log(2.0)));
}

std::complex<double> TOOLS_API z2f(double *fw, double *fb, double f_fs_2, unsigned long orderfw, unsigned long orderfb);
