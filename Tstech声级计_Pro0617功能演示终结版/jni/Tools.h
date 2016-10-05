#pragma once
#ifdef TOOLS_EXPORTS
#define  TOOLS_API
#else
#define  TOOLS_API
#endif
#include <math.h>
bool TOOLS_API GetRateFactors(unsigned long &num, unsigned long &den, double X, double tol = 1E-06, unsigned long n_max = 100000);
double TOOLS_API filter(double *fw, double *fb, double s, double *state, unsigned char orderfw = 1, unsigned char orderfb = 1);
void TOOLS_API GetWidmanCoeffs(double sampleRate, double *B, double tau_short = 0.005, double tau_long = 0.015, double tau_var = 0.075);
void TOOLS_API GetLowPassCoeffs(double *fw, double *fb, double fs, double f3db);
void TOOLS_API getHighCoeffs(double fw[2], double fb[2], double fs, double mf1, double f1, double mf2, double f2, double m0 = 1.0);
double TOOLS_API MakePostMasking(double data, double *state, double *B);
double TOOLS_API getSoundPressure(double db_spl);

inline double PatodB(double pa)
{
	return log10(pa / 0.00002) * 20.0;
}

inline double dBtoPa(double db)
{
	return pow(10.0, db / 20.0)*0.00002;
}

// Traunm¨¹llers transform from frequency in Hz to auditory scale "Tonheit" in Bark
inline double hz2bark_traunmueller(double hz)
{
	return 26.81*hz / (1960.0 + hz) - 0.53;//if(bark<0)bark=0;	
}

inline double hz2bark_baumann(double hz)
{
	double bark = hz2bark_traunmueller(hz);
	if (bark<2.0)
		bark = (2.0*bark + 1.06) / 2.53;
	else if (bark>20.1)
		bark = 1.22*bark - 4.422;
	return bark;
}

// Traunm¨¹llers transform from auditory scale "Tonheit" in Bark 
// to frequency in Hz 
inline double bark2hz_traunmueller(double bark)
{
	if (bark >= 26.27)
		bark = 26.27;
	return 1960.0*(0.53 + bark) / (26.28 - bark);
}

// Extension by Uwe Baumann to Traunmuellers transform from auditory 
// scale "Tonheit" to frequency in Hz 
inline double bark2hz_baumann(double bark)
{
	if (bark<2.0)
		bark = (bark*2.53 - 1.06) / 2.0;
	else if (bark>20.1)
		bark = (bark + 4.422) / 1.22;
	return bark2hz_traunmueller(bark);
}

// Critical bandwidth in Hz as a function of center frequency in Hz
// by Sottek based on Traunm¨¹llers auditory scale transform
inline double crb_traunmueller(double hz)
{
	hz = 1.96 + hz / 1000.0;
	return(hz*hz / 0.0525476);
}

// Critical bandwidth in Hz as a function of center frequency in Hz
// by Zwicker based on experimental data
inline double crb_Zwicker(double hz)
{
	return 25.0 + 75.0*pow(1.0 + 0.0000014*hz*hz, 0.69);
}

// Critical bandwidth in Hz as a function of center frequency in Hz
// by Terhardt.
inline double crb_terhardt(double hz)
{
	return (86.0 + 0.0055 * pow(hz, 1.4));
}

// Glasberg und Moore's transform from frequency to ERB scale
// based on erb(f)=24.7*(4.37/1000*f + 1 ) 
// erb:	number on the auditory ERB scale
inline double erb2hz(double erb)
{	/*	double Q, wQ;
	Q = 1000 / ( 24.7 * 4.37 );
	wQ = 1000 / 4.37;
	return ( wQ * ( exp( erb/Q ) - 1) );*/
	//	return ( 1000.0/4.37 * (exp(erb*24.7*4.37/1000.0) - 1) );
	return (228.83295194508 * (exp(erb*0.107939) - 1));
}

// Glasberg und Moore transform from ERB scale to frequency
// based on erb(f)=24.7*(4.37/1000*f + 1 ) 
// hz:	frequency in Hz
inline double hz2erb(double hz)
{	/*	double Q, wQ;
	Q = 1000 / ( 24.7 * 4.37 );
	wQ = 1000 / 4.37;
	return ( Q * log( 1 + hz/wQ ) );*/
	//return ( 1000.0/24.7/4.37 * log(hz*4.37/1000.0+1) );
	return (9.26449198158219 * log(hz*0.00437 + 1));
}

// Glasberg und Moore's auditory Equivalent Rectangular Bandwidth 
// (ERB) as a function of center frequency in Hz
inline double erb_Moore(double hz)
{	/*	double Q, minBW;
	minBW = 24.7;
	Q = 1000 / ( minBW * 4.37 );
	return ( minBW + hz/Q );*/
	//return ( 24.7*4.37*hz/1000.0 + 24.7 );
	return (0.107939 * hz + 24.7);
}


template <typename T>
inline void bufferpp(T **pointer, T *min, T *max)
{
	if (*pointer == max)
		*pointer = min;
	else
		(*pointer)++;
}


//inline void bufferpp(long **pointer, long *min, long *max)
//{
//	if (*pointer == max)
//		*pointer = min;
//	else
//		(*pointer)++;
//}

template <typename T>
inline void buffermm(T **pointer, T *min, T *max)
{
	if (*pointer == min)
		*pointer = max;
	else
		(*pointer)--;
}

// Absolute Threshold of Hearing in dB according to Terhardt as a function of 
// tone frequency.
inline double atc_Terhardt(double f)
{
	double fkHz = f / 1000.0, fkHz_33 = fkHz - 3.3;

	return (3.64*pow(fkHz, -0.8)
		- 6.5*exp(-0.6*fkHz_33*fkHz_33)
		+ 0.001*fkHz*fkHz*fkHz*fkHz);	// dB
}

template<typename T>
T clip(T value, T min, T max)
{
	if (value > max)
		return max;
	if (value < min)
		return min;
	return value;
}




