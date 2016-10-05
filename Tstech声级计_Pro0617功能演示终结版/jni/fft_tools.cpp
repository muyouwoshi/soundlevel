#define _USE_MATH_DEFINES
#include "math_ex.h"
#include "fft_tools.h"

#define MAX_NUM_POLYWIN_COEFFS 20

///////////////////////////////////////////////////////////////////
// Window functions for FFT applications
///////////////////////////////////////////////////////////////////

//! Hamming window	(0<=index<=N-1) 
double hamming(unsigned long index, unsigned long N)
{
	return(0.54 - 0.46*cos(2 * M_PI * index / (double)(N - 1)));
}

//! Hanning window (0<=index<= N-1)
double hanning(unsigned long index, unsigned long N, int flag)
{
	double w;
	if (flag == 1)
		w = 0.50 * (1 - cos(2 * M_PI * index / (double)(N - 1)));
	else	// is used by PSQM (although it is not symmetric)
		w = 0.50 * (1 - cos(2 * M_PI * index / (double)(N)));

	return w;
}

//! Kaiser-Bessel-Window (0<=index<=winlen-1)
double kaiser(double index, double winlen, double alpha, double bessel_alpha)
{
	if (bessel_alpha == 0)
		bessel_alpha = bessel(alpha);
	//double tmp = alpha*2/(double)(N-1) * sqrt((double)(index*(N-1-index)));
	//tmp = alpha*2/(double)(N-1) * sqrt((double)(index*(N-1-index)));
	winlen--;
	return bessel(alpha * 2 / winlen * sqrt(index*(winlen - index))) / bessel_alpha;
}

//! Kaiser Window for FIR Low Pass filtering (Interpolation/Anti-Aliasing) 
// with |index|<=winlen/2
double kaiser_(double index, double winlen, double alpha, double bessel_alpha)
{
	if (bessel_alpha == 0)
		bessel_alpha = bessel(alpha);
	return bessel(alpha*sqrt(1 - 4 * index*index / winlen / winlen)) / bessel_alpha;
}

//! Parameter for the Kaiser window as weighted ideal Low Pass (FIR filter)
double getKaiserCoeff(double stop_db, double *D)
{
	double d, alpha;

	if (stop_db < 0)
		stop_db = -stop_db;

	// alpha for the desired Attenuation
	if (stop_db <= 21)
	{
		alpha = 0;
		d = 0.992;
	}
	else
	{
		d = (stop_db - 7.95) / 14.36;
		if (stop_db < 50)
			alpha = 0.5842*pow(stop_db - 21, 0.4) + 0.07886*(stop_db - 21);
		else
			alpha = 0.1102*(stop_db - 8.7);
	}
	if (D != NULL)
		*D = d;

	return alpha;
}

//! Exponential decay window
double getExpCoeff(double end_db)
{
	double log_10 = 2.30258509299405;//natural log of 10

	// allow only decay
	if (end_db > 0)
		end_db = -end_db;

	return (-end_db*log_10 / 20.0);
}

//! Gauss-Window
double gausswin(double coeff, double index, double winlen)
{
	double x = coeff * (index / (winlen - 1) - 0.5);
	return (exp(-2.0 * x*x));
}

//! symmetrical exponential window
double expwin(double alpha, double index, double winlen)
{
	index -= (winlen - 1) / 2.0;
	if (index < 0)
		index = -index;

	return exp(-alpha*index * 2 / (winlen - 1));
}

//! Sidelobe Attenuation for Gausss-Window
double getGaussCoeff(double db)
{
	// parameters have been evaluated by numerical Matlab simuation
	double alpha;
	if (db <= 40)
		alpha = 2.401; // 41.6 dB //alpha = 2.5;	 
	else if (db <= 50)
		alpha = 2.766;
	else if (db <= 60)
		alpha = 3.134; //alpha = 3.16;
	else if (db <= 70)
		alpha = 3.442;
	else if (db <= 80)
		alpha = 3.737;//alpha = 3.765;
	else if (db <= 90)
		alpha = 4.046;
	else //if ( db <= 100 )
		alpha = 4.285;	//alpha = 4.32;
	return alpha;
}

//! Triangular Window  (0<=index<=N-1)
double bartlett(double index, double winlen)
{
	double x = index / (winlen - 1) - 0.5;
	if (x < 0)
		x = -x;
	return (1.0 - 2.0*x);
}

//! Polynomial window
int getBlackmanHarrisCoeffs(double *coeffs, double db)
{
	int num, n;
	double *row;

	if (db <= 62)
	{
		double tmp[3] = { 0.44959, 0.49364, 0.05677 };			// 62 dB
		num = 3;
		row = tmp;
	}
	else if (db <= 71)
	{
		double tmp[3] = { 0.42323, 0.49755, 0.07922 };			// 71 dB
		num = 3;
		row = tmp;
	}
	else if (db <= 74)
	{
		double tmp[4] = { 0.40217, 0.49703, 0.09892, 0.00188 };// 74 dB
		num = 4;
		row = tmp;
	}
	else if (db <= 92)
	{
		double tmp[4] = { 0.35875, 0.48829, 0.14128, 0.01168 };// 92 dB
		num = 4;
		row = tmp;
	}
	else	// exact Blackman
	{
		double tmp[3] = { 0.42659071, 0.49656062, 0.07684867 };
		num = 3;
		row = tmp;
	}

	for (n = 0; n < num; n++)
		coeffs[n] = row[n];

	return num;
}

//! Polynomial window as used for Nuttal, Flattop and Blackman-Harris
double polywin(double *coeffs, int num_coeffs, unsigned long index, double winlen)
{
	double sample = *coeffs;
	for (unsigned char n = 1; n < num_coeffs; n++)
		sample += coeffs[n] * cos(n * (2 * M_PI*index / (winlen - 1.0) - M_PI));

	return sample;
}

//! Flat Top Window designed by Prof. Manfred Zollner. 
int getFlatTopCoeffs(double *coeffs, double db)
{
	int n, num;
	double *row, max = 0;

	if (db <= 40)
	{
		double tmp[5] = { 1.0, 1.800, 0.560, -0.0190, 0.008000 };
		num = 5;
		row = tmp;
	}
	else if (db <= 50)
	{
		double tmp[5] = { 1.0, 1.710, 0.630, 0.0110, 0.006000 };
		num = 5;
		row = tmp;
	}
	else if (db <= 60)
	{
		double tmp[6] = { 1.0, 1.860, 0.903, 0.0868, -0.000080, -0.001900 };
		num = 6;
		row = tmp;
	}
	else if (db <= 70)
	{
		double tmp[5] = { 1.0, 1.850, 1.000, 0.1650, -0.000100 };
		num = 5;
		row = tmp;
	}
	else if (db <= 80)
	{
		double tmp[5] = { 1.0, 1.890, 1.135, 0.2597, 0.008450 };
		num = 5;
		row = tmp;
	}
	else if (db <= 90)
	{
		double tmp[5] = { 1.0, 1.933, 1.286, 0.3880, 0.032830 };
		num = 5;
		row = tmp;
	}
	else //if ( db <= 100 )
	{
		double tmp[7] = { 1.0, 1.906, 1.272, 0.4073, 0.040500, 0.000183, -0.0000126 };
		num = 7;
		row = tmp;
	}

	for (n = 0; n < num; n++)
		max += row[n];

	for (n = 0; n < num; n++)
		coeffs[n] = row[n] / max;

	return num;
}

//! Polynomial window according to Prof. Manfred Zollner
int getNuttallCoeffs(double *coeffs, double db)
{
	int n, num;
	double *row;

	if (db <= 47)
	{
		double tmp[3] = { 0.3750000, 0.5000000, 0.125000 };	//;{3/8.0, 4/8.0, 1/8.0};
		row = tmp;
		num = 3;
	}
	else if (db <= 61)
	{
		double tmp[4] = { 0.3125000, 0.4687500, 0.1875000, 0.0312500 };//{ 10/32.0, 15/32.0, 6/32.0, 1/32.0 }; 
		row = tmp;
		num = 4;
	}
	else if (db <= 64)
	{
		double tmp[3] = { 0.4089700, 0.5000000, 0.0910300 };
		row = tmp;
		num = 3;
	}
	else if (db <= 83)
	{
		double tmp[4] = { 0.3389460, 0.4819730, 0.1610540, 0.0180270 };
		row = tmp;
		num = 4;
	}
	else if (db <= 93)
	{
		double tmp[4] = { 0.3557680, 0.4873960, 0.1442320, 0.0126040 };
		row = tmp;
		num = 4;
	}
	else //if ( db <= 98 )
	{
		double tmp[4] = { 0.3635819, 0.4891775, 0.1365995, 0.0106411 };
		row = tmp;
		num = 4;
	}

	for (n = 0; n < num; n++)
		coeffs[n] = row[n];

	return num;
}

/*! Flat window with cosine slopes
* pc is the realtive duration according to the total duration
*/
double tukey(double pc, long index, double winlen)
{
	double w = 1;

	if (pc < 0)
		pc = 0;
	else if (pc > 1)
		pc = 1;

	long coslen = (long)(pc*winlen);
	if (coslen / 2 * 2 != coslen)
		coslen--;

	if (index < coslen / 2)	// einschwingen
	{
		w = 0.5 * (1 + cos(-M_PI + index / (double)(coslen - 1) * 2 * M_PI));
	}
	else
	{
		index -= (long)(winlen - coslen);
		if (index >= coslen / 2)
			w = 0.5 * (1 + cos(-M_PI + index / (double)(coslen - 1) * 2 * M_PI));
	}

	return w;
}

//! Parameter for Kaiser window
double getKaiserCoeff(double main_side_db)
{
	double alpha;// c = 12*(main_side_db+12)/155.0; win_main_df_fs = c/(double)(winlen-1);

	if (main_side_db <= 13.26)
		alpha = 0;	// Rectangular
	else if (main_side_db <= 60)
		alpha = 0.76609 * pow(main_side_db - 13.26, 0.4) + 0.09834 * (main_side_db - 13.26);
	else
		alpha = 0.12438 * (main_side_db + 6.3);

	return alpha;
}

double TOOLS_API getWindow(double *window, uint32_t winlen, WindowType type, double winpar /*= 0*/)
{
	unsigned long k;
	double coeffs[MAX_NUM_POLYWIN_COEFFS];
	int n = 0;

	if (winlen == 1)
		*window = 1;
	else if (type == HAMMING)
	{
		for (k = 0; k < winlen; k++)
			window[k] = hamming(k, winlen);
		winpar = 42.7;  // Side Lobe Attenuation
	}
	else if (type == HANNING)
	{
		for (k = 0; k < winlen; k++)
			window[k] = hanning(k, winlen, 1);
		winpar = 31.5; // Side Lobe Attenuation
	}
	else if (type == EXPONENTIAL)
	{
		double coeff = getExpCoeff(winpar);
		for (k = 0; k < winlen; k++)
			window[k] = expwin(coeff, k, winlen);
	}
	else if (type == GAUSS_WINDOW)
	{
		double coeff = getGaussCoeff(winpar);
		for (k = 0; k < winlen; k++)
			window[k] = gausswin(coeff, k, winlen);
	}
	else if (type == BARTLETT)
	{
		for (k = 0; k < winlen; k++)
			window[k] = bartlett(k, winlen);
		winpar = 26.5; // Side Lobe Attenuation
	}
	else if (type == BLACKMAN_HARRIS)
	{
		n = getBlackmanHarrisCoeffs(coeffs, winpar);
		for (k = 0; k < winlen; k++)
			window[k] = polywin(coeffs, n, k, winlen);
	}
	else if (type == FLAT_TOP)
	{
		n = getFlatTopCoeffs(coeffs, winpar);
		for (k = 0; k < winlen; k++)
			window[k] = polywin(coeffs, n, k, winlen);
	}
	else if (type == NUTTALL)
	{
		n = getNuttallCoeffs(coeffs, winpar);
		for (k = 0; k < winlen; k++)
			window[k] = polywin(coeffs, n, k, winlen);
	}
	else if (type == KAISER_BESSEL)
	{
		double alpha = getKaiserCoeff(winpar), bessel_alpha = bessel(alpha);;
		for (k = 0; k < winlen; k++)
			window[k] = kaiser(k, winlen, alpha, bessel_alpha);
	}
	else if (type == RECTANGULAR)
	{
		for (k = 0; k < winlen; k++)
			window[k] = 1.0;
		winpar = 13.3;	// Side Lobe Attenuation
	}
	else if (type == TUKEY)
	{
		for (k = 0; k < winlen; k++)
			window[k] = tukey(winpar, k, winlen);
	}

	return winpar;
}

//! compute FFT scaling for the power domain
void getFFTScale(double *window, uint32_t winlen, uint32_t fftlen, double& scale_magnitude, double& scale_power)
{
	// Power of the window
	double Pwin = 0.0, Mwin = 0.0, w;

	for (uint32_t c = 0; c < winlen; c++)
	{
		w = window[c];
		// Power Density Scaling:
		// Power factor, to fulfill Parseval's Theorem:
		// Time sequence power=sum of all power density spectral lines
		Pwin += w * w;

		// Magnitude Scaling:
		// power factor, so that the amplitude of a tone
		// is computed right
		Mwin += w;
	}

	scale_magnitude = 1.0 / Mwin / Mwin;// / FFT->winlen / FFT->winlen;
	scale_power = 1.0 / Pwin / (double)fftlen;
}

