// Tools.cpp : Defines the exported functions for the DLL application.
//

#define _USE_MATH_DEFINES
#include "Tools.h"
#include <math.h>
#include "math_ex.h"

bool GetRateFactors(unsigned long &num, unsigned long &den, double X, double tol, unsigned long n_max)
{
	unsigned long n;//    % [n(k) n(k-1); d(k) d(k-1)];
	double  C11 = 1, C12 = 0, C21 = 0, C22 = 1, x = X, D11, D21;//[][2] = { {1, 0}, {0, 1} };
	long d;

	tol *= X;
	for (n = 0; n < n_max; n++)
	{
		d = static_cast<long>(round(x));
		x -= d;

		D11 = C11;
		D21 = C21;

		C11 = C11*d + C12;
		C21 = C21*d + C22;
		C12 = D11;
		C22 = D21; //C = [[x;0] C(:,1)];

		if (x == 0.0 || fabs(C11 / C21 - X) <= tol)
			break;

		x = 1.0 / x;
	}

	num = static_cast<unsigned long>(fabs(C11));
	den = static_cast<unsigned long>(fabs(C21));

	if (n == n_max)
		return false;

	return true;
}

/*! canonical direct filter implementation [ Sophocles, 1996 ]
* *fw, *fb: pointer to forward and feedback filter coefficients in a similar form
* as used for the Matlab filter function (fb[0] is assumed to be equal to 1)
* \param fw
* \param fb
* \param s input sample
* \param state
* \param orderfw
* \param orderfb
*/
double filter(double *fw, double *fb, double s, double *state, unsigned char orderfw, unsigned char orderfb)
{
	// fb[0] = 1
	unsigned char c;
	for (c = 1; c <= orderfb; c++)		// input adder
		s -= fb[c] * state[c];
	*state = s;//state[0] = s;

	s *= (*fw);
	for (c = 1; c <= orderfw; c++)		// output adder
		s += fw[c] * state[c];

	c = orderfb;
	if (orderfb < orderfw) //orderfb == 0
		c = orderfw;	// f¨¹r FIR Filter

	// reverse state updating
	for (; c >= 1; c--)
		state[c] = state[c - 1];

	return s;
}

//! Time Constants for exponential decay
void GetWidmanCoeffs(double fs, double *B, double tau_short/*=0.005*/, double tau_long/*=0.015*/, double tau_var/*=0.075*/)
{
	double lambda1, lambda2, p, q, den, e1, e2, dt;

	// Time Constants for exponential decay
	// double tau_short = 0.005, tau_long = 0.015, tau_var = 0.075;

	dt = 1.0 / fs;
	p = (tau_var + tau_long) / (tau_var*tau_short);
	q = 1.0 / (tau_var*tau_short);

	lambda1 = -p / 2. + sqrt(p*p / 4. - q);
	lambda2 = -p / 2. - sqrt(p*p / 4. - q);

	den = tau_var * (lambda1 - lambda2);

	e1 = exp(lambda1 * dt);
	e2 = exp(lambda2 * dt);

	B[0] = (e1 - e2) / den;
	B[1] = ((tau_var * lambda2 + 1) * e1
		- (tau_var * lambda1 + 1) * e2) / den;
	B[2] = ((tau_var * lambda1 + 1) * e1 -
		(tau_var * lambda2 + 1) * e2) / den;
	B[3] = (tau_var * lambda1 + 1) *
		(tau_var * lambda2 + 1) * (e1 - e2) / den;
	B[4] = exp(-dt / tau_long);
	B[5] = exp(-dt / tau_var);
}

//! First order low pass (Bilinear transform) 
void GetLowPassCoeffs(double *fw, double *fb, double fs, double f3db)
{
	// G(s) = a/(s+a)  s = (z-1)/(z+1)  omega_analog = tan( pi*f/fs ), a = omega_analog
	// G(z) = a/(a+1) * (z+1)/(z+(a-1)/a+1);
	double wcut = tan(M_PI*f3db / fs), factor;
	factor = wcut / (wcut + 1);

	fw[0] = factor;
	fw[1] = factor;
	fb[0] = 1;
	fb[1] = (wcut - 1) / (wcut + 1);
}

// parametrical first order high pass
// fw[2], fb[2]:	first order forward and feedback coefficients
// fs:				sample rate
// mf1:				magnitude at frequeny 1
// f1:				frequency 1 (Hz)
// mf2:				magnitude at frequency 2
// f2:				frequency 2 (Hz)
// m0:				magnitude at 0 Hz
void getHighCoeffs(double fw[2], double fb[2], double fs, double mf1, double f1, double mf2, double f2, double m0)
{
	// prewarping
	f1 = fs / M_PI * tan(f1 / fs*M_PI);
	f2 = fs / M_PI * tan(f2 / fs*M_PI);

	// H(s) = (a+b*s)/(1+c*s), a=m0
	double omega1 = 2 * M_PI*f1, omega2 = 2 * M_PI*f2, kappa = 2 * fs, a = m0, b, c;

	c = sqrt(fabs(1.0 / omega1 / omega1 * (mf1*mf1 - m0*m0) / (mf2*mf2 - mf1*mf1) -
		1.0 / omega2 / omega2 * (mf2*mf2 - m0*m0) / (mf2*mf2 - mf1*mf1)));

	b = sqrt(fabs(mf1*mf1 * (1 + c*c*omega1*omega1) - m0*m0)) / omega1;

	fw[0] = (a + b*kappa) / (1.0 + c*kappa);
	fw[1] = (a - b*kappa) / (1.0 + c*kappa);
	fb[0] = 1.0;
	fb[1] = (1.0 - c*kappa) / (1.0 + c*kappa);
}

/* Algorithm for simulating an exponential decay with two time constants
as function of duration.
Reference: U. Widmann, R. Lippold, H. Fastl:
A computer program simulating post-masking for applications in
sound analysis systems. NOISE-Con 98, Ypsilanti, Michigan, 1998.*/
double MakePostMasking(double data, double *state, double *B)
{
	double uC1, uC2;

	if (data < state[0])
	{
		if (state[0] > state[1])
		{
			uC2 = state[0] * B[0] - state[1] * B[1];
			uC1 = state[0] * B[2] - state[1] * B[3];

			if (data > uC1)
				uC1 = data;
			if (uC2 > uC1)
				uC2 = uC1;
		}
		else
		{
			uC1 = state[0] * B[4];

			if (data > uC1)
				uC1 = data;

			uC2 = uC1;
		}
	}
	else if (data == state[0])
	{
		uC1 = data;
		if (uC1 > state[1])
			uC2 = (state[1] - data) * B[5] + data;
		else
			uC2 = data;
	}
	else
	{
		uC1 = data;
		uC2 = (state[1] - data) * B[5] + data;
	}

	state[0] = uC1;
	state[1] = uC2;

	return uC1;
}

double getSoundPressure(double db_spl)
{
	return pow(10.0, db_spl / 20.0)*0.00002;
}




