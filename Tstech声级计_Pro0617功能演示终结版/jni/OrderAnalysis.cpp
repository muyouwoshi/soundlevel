// OrderAnalysis.cpp : Defines the exported functions for the DLL application.
//

#include "OrderAnalysis.h"
#include <memory>
#define _USE_MATH_DEFINES
#include <math.h>
#include <complex>
#include <vector>
#include <algorithm>
#include <stdint.h>
#include <thread>
#ifdef _MSC_VER
#include <fft.h>
#include <math_ex.h>
#include <rpm.h>
#else
#include <fft_tools.h>
#include <math_ex.h>
#include <rpm.h>
#endif

using namespace std;

const double MAX_24BIT = 8388608;
const double OVERSAMPLING_FACTOR = 4;
const uint32_t FFT_SIZE_MAX = 2048;
const double ORDER_FIR_DF_FS = 0.1;
const double KAISER_BESSEL_DFT_PAR = 60;
const double FIR_DB_STOP = 80;
const uint32_t SIN_X_X_SAMPLES_PER_CROSS = 512;					// num.of samples per zero-crossing of sin(x)/x table  
const uint32_t KAISER_WINLEN_HALF_MAX = 256;					// max.number of half of the samples of the Kaiser window
const double MIN_DB = -300;


/*! Chirp Transformation : Evaluation of the Z-Transform on the unit circle in equidistant angles
* \verbatim
* X(Wk) = X(z=Wk) = d(k) * conv(y(n)*c(n))(k), k = 0,..,freqs-1 and
* Wk=W0 + k*dW = 2*pi*(f0_fs + k*df_fs)
*
* d(k) is the demodulation term and is only used for phase computation (not magnitude).
* d(k) = exp(-j*dW/2*k*k).
*
* c(n) is called a chirp, since it is a harmonic with linear increasing frequency
* c(n) = exp(j*dW/2*n*n) , n=-Inf,...,+Inf
*
* y(n) is the input signal x(n) complex modulated by m(n) ;n = 0,...,samples-1
* y(n) = x(n) * m(n) with m(n)=exp(-j*(W0*n+dW/2*n*n) )
*
* Since y(n) is only in the range n=0,..,samples-1 not zero, c(n) is only
* evaluated for n=-(samples-1),...,freqs-1 (samples+freqs-1 values)(FIR Filter).
*
* \endverbatim
* Die Convolution of y(n) with c(n) is done using FFT in the frequency domain.
*/
void dft_set(complex<double> * mod, complex<double> *fft_chirp, double f0_fs, double df_fs,
	unsigned long winlen, unsigned long freqs, unsigned long fftlen)
{
	// FIR Size
	unsigned long n, chirplen = winlen + freqs - 1;
	double phi, tmp, omega0 = 2 * M_PI*f0_fs, domega_2 = M_PI*df_fs;
	complex<double> c0, *ctmp;
	// complexe Input Modulator m(n)
	for (n = 0; n < winlen; n++, mod++)
	{
		phi = omega0 * n + domega_2 * n * n;
		mod->real(cos(phi));
		mod->imag(-sin(phi));
	}
	for (; n < fftlen; n++, mod++)
		*mod = c0;//.real=0;

	// Chirp c(n)
	ctmp = fft_chirp;
	for (n = 0; n < chirplen; n++, ctmp++)
	{
		tmp = 1.0 - winlen + n;
		phi = domega_2*tmp*tmp;
		ctmp->real(cos(phi));
		ctmp->imag(sin(phi));
	}
	for (; n < fftlen; n++, ctmp++)
		*ctmp = c0;

	// FFT of c(n) for convolution
	fft(fftlen, fft_chirp);
}

/*! Computation of the chirp Transform evaluated on the unit circle
* The resulting pointer points to the begin of the result in array data
*/
complex<double> * dft(complex<double> *data, complex<double> *mod, complex<double> *fft_chirp, uint32_t winlen,
	uint32_t freqs, uint32_t fftlen)
{
	// complex modulation of real signal
	uint32_t k;
	for (k = 0; k<winlen; k++)
	{
		data[k].imag(mod[k].imag()*data[k].real());
		data[k].real(data[k].real() * mod[k].real());
	}
	// pad the rest with zeros
	for (; k<fftlen; k++)
	{
		data[k].real(0);
		data[k].imag(0);
	}

	// FFT of the complex modulated input signal for the convolution with the chirp 
	fft(fftlen, data);

	// product with the FFT of the chirp and complex conjugation for the 
	// following inverse FFT 
	complex<double> *datap = data;
	double real;
	for (k = 0; k<fftlen; k++)
	{
		real = datap->real();
		datap->real(real*fft_chirp[k].real() - datap->imag()*fft_chirp[k].imag());
		datap->imag(-real*fft_chirp[k].imag() - datap->imag()*fft_chirp[k].real());
		datap++;
	}

	// FFT of inverse FFT 
	fft(fftlen, data);

	// Conjugation for IFFT and scaling in the needed range
	double divr = fftlen, divi = -(double)fftlen;
	datap = &data[winlen - 1];
	for (k = 0; k<freqs; k++)
	{
		datap[k].real(datap[k].real() / divr);
		datap[k].imag(datap[k].imag() / divi);
	}
	return datap;
}

/*! computes the phase of a sine sweep with linearly increasing frequency
* between f0 and f1 in the time T (square phase function starting with phi0 at time=0)
* at the desired times (>=0). Used for Order analysis.
*
* omega(t) = omega0 + k*t;
* phi(t) = omega0*t + k/2*t*t + phi0;
* t*t + omega0*2/k*t + (phi-phi0)*2/k = 0
* t*t + p*t          + q              = 0
* t = -p/2 + sqrt(p*p/4-q)
*/
double lin_sweep_phase(double time, double f0, double f1, double T, double phi0 = 0)
{
	if (T <= 0)
		return phi0;

	f0 *= (2 * M_PI);//omega0=2*pi*f0;
	f1 *= (2 * M_PI);//omega1=2*pi*f1;

	double k = (f1 - f0) / T;
	return (f0*time + k / 2.0*time*time + phi0);
}

/*! computes the times of a sine sweep with linearly increasing frequency
*  between f0 and f1 in the time T (square phase function starting with phi0 at time=0)
*  at the desired phases. Used for Order analysis.
*/
double lin_sweep_time(double phi, double f0, double f1, double T, double phi0 = 0)
{
	f0 *= (2 * M_PI);//omega0=2*pi*f0;
	f1 *= (2 * M_PI);//omega1=2*pi*f1;

	if (phi <= phi0)
		return 0;
	else if (f0 == f1 && f0>0) // no sweep 
		return ((phi - phi0) / f0);
	else if (T>0)
	{
		double k = (f1 - f0) / T;
		if (k>0)
			return (-f0 / k + sqrt(f0*f0 / k / k + 2 * (phi - phi0) / k));
		else
			return (-f0 / k - sqrt(f0*f0 / k / k + 2 * (phi - phi0) / k));
	}
	return 0;
}


class COrderAnalysis::COrderAnalysisImpl
{
public:
	explicit COrderAnalysisImpl(OrderParams params)
		: m_param(params)
		, m_rpm_rate(0.0f)
		, m_nProgress(-1)
	{
		Init();
	}
	~COrderAnalysisImpl()
	{
		if (m_calc_thread && m_calc_thread->joinable())
			m_calc_thread->join();
	}
	void SetRPMData(const float* rpm, int count, float samplerate);
	int GetProgress()
	{
		return m_nProgress;
	}
	uint32_t GetResultSize()
	{
		if (!m_rpm_calc.HaveRPMData())
			return 0;
		uint32_t start, stop, delta;
		m_rpm_calc.GetRPMRange(start, stop, delta);
		return (stop - start) / delta + 1;
	}
	bool GetResult(double order, float* resultBuffer)
	{
		if (order > m_param.maxOrder || order < m_param.minOrder)
			return false;
		uint32_t index = (uint32_t)((order - m_param.minOrder) / m_param.spacing);
		if (index >= m_order_channels)
			index = m_order_channels - 1;
		vector<float> time_data;
		for (auto& result : m_results)
			time_data.push_back((float)pow(10, result[index] /10));
		auto vec = m_rpm_calc.GetRPMData(time_data.begin(), time_data.end(), m_param.time_interval);
		for (auto& data : vec)
			data = 10 * log10(data);
		memcpy(resultBuffer, vec.data(), vec.size() * sizeof(float));
		return true;
	}
	bool GetResultRPMInfo(uint32_t& startRPM, uint32_t& stopRPM, uint32_t& deltaRPM)
	{
		return m_rpm_calc.GetRPMRange(startRPM, stopRPM, deltaRPM);
	}
	template<typename T>
	bool Calculate(const T* data, int samples)
	{
		m_nProgress = -1;
		if (m_calc_thread != nullptr)
			return false;
		if (!InitOrder())
			return false;
		m_signal_data.clear();
		m_signal_data.reserve(samples);
		for (int i = 0; i != samples; ++i)
		{
			m_signal_data.push_back((double)data[i]);
		}
		m_total_frame = (uint32_t)(samples * 1000 / (m_param.samplerate * m_param.time_interval));
		if (m_total_frame == 0)
			m_total_frame = 1;
		m_nProgress = 0;
		m_calc_thread.reset(new thread([this]()
		{
			double time, time0, time1, time01, rps0, rps1, phi, phi1, rps;
			uint32_t rps_index = 0;
			for (uint32_t i = 0; i != m_total_frame; ++i)
			{
				// absolute start time of this order spectrum
				time = i * m_param.time_interval / 1000.0;//(unsigned long)((k*ORDER.dt+PAR->PAR.t0)*SOUND_SAMPLE_RATE+0.5)/SOUND_SAMPLE_RATE;//

				// find the Rotation speed samples (and corresponding times) left and right of the time
				rps1 = getNextRps(time, rps_index, time1);
				rps0 = getPrevRps(time, rps_index, time0);

				// Duration of this rotation interval (rotation speed is interpolated linearly in this interval)
				time01 = time1 - time0;// RPM_CHUNK_DT;//duration = (stop-start)/fs;

				// angle at time-time0 with angle=0 at time0
				phi = lin_sweep_phase(time - time0, rps0, rps1, time01);
				phi1 = lin_sweep_phase(time01, rps0, rps1, time01);		// angle at the end of the rotation interval

				// interpolate samples at an equidistant angle interval and store them in the window
				for (uint32_t w = 0; w < m_dft_win_length; w++, phi += m_delta_phi)
				{
					while (phi > phi1)
					{	// next rotation interval (outside the current range)

						// IM 1528 (bug order)
						if (phi1 <= 0)
							return;

						rps0 = rps1;
						time0 = time1;
						phi -= phi1;
						rps1 = getNextRps(time0, rps_index, time1);
						time01 = time1 - time0;
						phi1 = lin_sweep_phase(time01, rps0, rps1, time01);
					}

					// find the time for the required sample
					time = time0 + lin_sweep_time(phi, rps0, rps1, time01);

					// current rotation frequency for Anti-Alias Filter in getSample
					rps = rps0 + (rps1 - rps0)*(time - time0) / time01;

					// interpolate sound sample and weight with the window
					m_dft_data[w].real(getSample(time, rps) * m_dft_window[w]);//DFT_DATA[w].imag = 0;
					m_dft_data[w].imag(0);
				}

				// compute power order spectrum
				getDFTpower();
				m_nProgress = (i + 1) * 100 / m_total_frame;
			}
			m_nProgress = 1000;
		}));
		return true;
	}

private:
	// evaluate the power spectrum with the optimum method
	void getDFTpower()
	{
		uint32_t w;
		complex<double> * spect;
		// FFT mode
		if (m_fft_flag)
		{
			if (m_dft_win_length > m_fft_size)
			{	// DFT_WIN_LENGTH is greater than the FFT size by an integer factor
				// -> use FFT Algorithm
				uint32_t s = 0, W = m_dft_win_length / m_fft_size - 1;
				spect = &m_dft_data[m_fft_size];
				for (w = 0; w < W; w++)
				{	// add overlapping Sound Segments (segment shift is FFT_SIZE)
					for (s = 0; s < m_fft_size; s++)
						m_dft_data[s].real( m_dft_data[s].real() + spect[s].real());
					spect += m_fft_size;
				}
			}
			// compute spectrum of the real signal
			realDataFFT(m_dft_data.get(), m_fft_size);//singlefft( FFT_SIZE, DFT_DATA );
			// find index of the smallest desired analysis order
			w = (uint32_t)(m_param.minOrder / m_param.spacing);
			spect = &m_dft_data[w];
		}
		else // fast DFT
			spect = dft(m_dft_data.get(), m_dft_modul.get(), m_dft_chirp.get(), m_dft_win_length, m_order_channels, m_fft_size);

		// compute the power spectrum from the complex spectrum in the desired order range
		w = 0;
		if (m_param.minOrder == 0)
		{
			spect[w].real(spect[w].real() * spect[w].real());
			w++;
		}

		for (; w < m_order_channels; w++)
			spect[w].real(2 * norm(spect[w]));
		unique_ptr<double[]> result(new double[m_order_channels]);
		for (uint32_t i = 0; i != m_order_channels; ++i)
		{
			if (spect[i].real() < m_min_power_ref)
				spect[i].real(m_min_power_ref);
			result[i] = 10 * log10(spect[i].real() / m_power_ref);
		}
		m_results.push_back(std::move(result));
	}
	// Spectrum (not power spectrum ) of a real signal using the symmetry 
	// properties of the FFT for a real signal. 
	// The real signal is placed in the real part of the complext array  
	void realDataFFT(complex<double> *realData, unsigned long fft_size)
	{
		unsigned long k = 0, n = fft_size / 2;
		complex<double> *data = realData, *tmp1 = data, even, odd, w, *tmp2 = NULL;
		for (; k < n; k++, tmp1 += 2)
		{
			data[k].real(tmp1->real());
			data[k].imag(tmp1[1].real());
		}
		fft(n, data);
		tmp1 = &data[n];
		tmp2 = tmp1;
		data[n] = data[0];

		double arg = 2 * M_PI / (double)fft_size, phi = 0.0;
		for (k = 0; k < n; k++, tmp1--, tmp2++, phi = arg*k)
		{
			even.real(data[k].real() + tmp1->real());
			even.imag(data[k].imag() - tmp1->imag());

			odd.real(data[k].imag() + tmp1->imag());
			odd.imag(tmp1->real() - data[k].real());

			w.real(cos(phi));
			w.imag(-sin(phi));

			odd *= w;
			even -= odd;
//			cmpmul_(&odd, w);
//			cmpsub_(&even, odd);

			tmp2->real(even.real()*0.5);
			tmp2->imag(even.imag()*0.5);
		}
		data->real(data->real() + data->imag());
		data->imag(0);

		tmp2--;
		for (k = 1; k < n; k++, tmp2--)
		{
			data[k].real(tmp2->real());
			data[k].imag(-tmp2->imag());
		}
	}
	bool InitOrder()
	{
		if (!InitRotationData())
			return false;
		if (!m_rpm_calc.HaveRPMData())
			return false;
		// integer num. of full cycles per DFT to realize the required order resolution (based on rectangle window)
		m_cycles_per_dft = (uint32_t)ceil(1.0 / m_param.resolution);

		// realized order resolution dO = 1/R, R:cyclesPerDFT
		m_order_res = 1.0 / m_cycles_per_dft;

		// determine DFT window length and appropriate FFT size
		m_fft_size = GetFFTSize(m_dft_win_length);

		// Angle sample interval (radiants), R=N*dr, dphi=2*pi*dr, dr=R/N; N:windowLength, dr: cycle sample interval 
		m_delta_phi = 2 * M_PI / (m_order_res * m_dft_win_length);//=2*PI*ORDER.cycles_per_dft/DFT_WIN_LENGTH

		// Nyquist order due to the angle sample interval: Osamp=1/dr, Onyq=Osamp/2=1/dr/2=2*pi/dphi/2=pi/dphi
		m_nyquist_order = M_PI / m_delta_phi;

		// max.valid FFT size is not sufficient to analyse the max.desired order with th required resolution
		if (m_nyquist_order < m_param.maxOrder)
		{
			m_param.maxOrder = (float)m_nyquist_order;
			if (m_param.minOrder >= m_param.maxOrder)
				m_param.minOrder = 0;
			m_order_channels = (uint32_t)((m_param.maxOrder - m_param.minOrder) / m_param.spacing) + 1;
//			m_param.spacing = (m_param.maxOrder - m_param.minOrder) / m_order_channels;
		}

		// Cutoff-Order of the Kaiser Anti-Alias-Lowpass, so that orders which are				// damit die in den analysierten Ordnungsbereich 
		// aliased into the analysed order range are attenuated at least by ORDER.fir_db_stop	// eingespiegelten Komponenten mindestens um ORDER.fir_db_stop ged?mpft werden
		m_cutoff_order = (2 * m_nyquist_order - m_param.maxOrder) / (1.0 + ORDER_FIR_DF_FS);

		// init DFT/FFT computation
		if (!InitDFT())
			return false;

		// init KAISER Lowpass/Anti-Alias filter
		if (!InitKaiserLowPass())
			return false;

		return true;
	}
	bool InitRotationData()
	{
		if (m_rpm_data.empty() || m_rpm_rate == 0)
			return false;
		auto minmax = minmax_element(m_rpm_data.begin(), m_rpm_data.end());
		m_rpm_min = *minmax.first;
		m_rpm_max = *minmax.second;
		m_rpm_dt = 1.0 / m_rpm_rate;
		return true;
	}
	// find the window length and the appropriate FFT size dependent on the desired order spacing and bandwidth
	uint32_t GetFFTSize(uint32_t &winlen)
	{
		// Minimum window length to realize the given order resolution(ORDER_RES, defined by the full num.of cycles 
		// per DFT) and the max.analysis order (which is half the cycle sample rate)
		double order = OVERSAMPLING_FACTOR * m_param.maxOrder, order_max = m_param.samplerate / m_rpm_min / 2.0, order_min = 25;
		if (order < order_min)
			order = order_min;
		if (order > order_max)
			order = order_max;
		// windowLengthN=numOfFullCycles/cycleSampleInterval=R/dr=R*Osamp=R*2*Onyq=2*Onyq/dO
		winlen = (uint32_t)ceil(2 * order / m_order_res);

		// Conditions for the use of the FFT algorithm:
		// 1. min.analsis order is an integer Multiple of the order spacing (delta_order)
		m_fft_flag = ((uint32_t)(m_param.minOrder / m_param.spacing) * m_param.spacing == m_param.minOrder);

		// 2. delta_order (order spacing) is an integer multiple of the order resolution ORDER_RES
		// for decimation_factor>1 a decimated DFT is applied, e.g. the window length 
		// is greater than the FFT size by an integer factor. 
		uint32_t decimation_factor = (uint32_t)(m_param.spacing / m_order_res);
		m_fft_flag = (m_fft_flag && decimation_factor * m_order_res == m_param.spacing);

		// Otherwise: a fast DFT algorithm (using FFT convolution) is applied
		unsigned long fft_size = 1, min_fft_size;
		if (!m_fft_flag) // DFT Algorithm
			min_fft_size = winlen + m_order_channels - 1;
		else // (decimated) FFT Algorithm
			min_fft_size = (unsigned long)ceil((double)(winlen / decimation_factor));

		// next power of 2
		while (fft_size < min_fft_size)
			fft_size *= 2;

		// check FFT size limit
		if (fft_size > FFT_SIZE_MAX)
			fft_size = FFT_SIZE_MAX;

		// optimum window length for the estimated FFT size
		if (m_fft_flag) // FFT Algorithmus
			winlen = fft_size*decimation_factor;
		//else winlen = fft_size-ORDER.channels+1;

		return fft_size;
	}

	// DFT Window with a mean value (DC) normalized to 1.0 (acc.to a band filter with max.response of 1) 
	void GetDFTwindow(double *window, uint32_t winlen, WindowType wintype)
	{
		unsigned long k = 0;
		if (wintype == RECTANGULAR)
		{
			double w = 1.0 / (double)winlen;
			for (k = 0; k < winlen; k++)
				window[k] = w;
		}
		else
		{
			getWindow(window, winlen, wintype, KAISER_BESSEL_DFT_PAR);
			double sum = 0;
			if (wintype == HANNING)
				sum = 0.5*winlen;
			else for (k = 0; k < winlen; k++)
				sum += window[k];
			for (k = 0; k < winlen; k++)
				window[k] /= sum;
		}
	}
	// Allocates all arrays which are needed for the Chirp z-Transform and returns 
	// the FFT size for the convolution
	uint32_t dft_alloc(uint32_t winlen, uint32_t freqs)
	{
		uint32_t fftlen = 1, chirplen = winlen + freqs - 1;

		while (fftlen < chirplen)
			fftlen *= 2;
		m_dft_data.reset(new complex<double>[fftlen]);
		m_dft_modul.reset(new complex<double>[fftlen]);
		m_dft_chirp.reset(new complex<double>[fftlen]);
		return fftlen;
	}
	// Dependent on the parameters either a non-power of 2 DFT Algorithm (based on Chirp Z-Transform) 
	// with FFT Convolution, the standard FFT Algorithm or the decimated DFT Algorithm is applied on the weighted data. 
	// For the DFT the order spacing as well as the min.analysis order can be chosen seperately.
	bool InitDFT()
	{
		m_dft_window.reset(new double[m_dft_win_length]);
		// compute the window
		GetDFTwindow(m_dft_window.get(), m_dft_win_length, m_param.type);

		// komplex array for FFT erzeugen (standard or decimated FFT)
		if (m_fft_flag)
		{
			m_dft_data.reset(new complex<double>[m_dft_win_length]());
			return true;
		}

		// complex array for DFT algorithm (Chirp Z-Transform)
		if (dft_alloc(m_dft_win_length, m_order_channels) != m_fft_size)
			return false;

		// Define the complex arrays required for the the Chirp Z-Transform
		dft_set(m_dft_modul.get(), m_dft_chirp.get(), m_param.minOrder / (m_nyquist_order * 2),
			m_param.spacing / (m_nyquist_order * 2), m_dft_win_length, m_order_channels, m_fft_size);

		return true;
	}
	// Init the Kaiser-Bessel FIR filter for Interpolation and Anti-Alias filtering 
	// for the data resampling acc. to a constant cycle sample interval
	bool InitKaiserLowPass()
	{
		// Kaiser Lowpass (sin(x)/x) weighted by the Kaiser Window) with the desired stop-band attenuation
		double kaiser_parameter = 0.1102 * (FIR_DB_STOP - 8.7);
		// modified Bessel-function of first type and 0th order as a function of the KAISER_PARAMETER
		double kaiser_bessel_parameter = bessel(kaiser_parameter);

		// (uneven) required window length for the desired transition width
		// between stop and pass band
		m_kaiser_winlen = (uint32_t)(ceil((FIR_DB_STOP - 7.95) / 14.36 / ORDER_FIR_DF_FS) + 1);
		m_kaiser_winlen_half = (uint32_t)ceil(m_kaiser_winlen / 2.0);
		m_kaiser_winlen = 2 * m_kaiser_winlen_half + 1;

		// Generate a table with weighted sin(x)/x values. The lowpass is achieved by weighting the 
		// ideal lowpass impulse response (sin(x)/x) with the Kaiser window
		m_sin_x_x_samples = (uint32_t)(m_kaiser_winlen / 2.0 * SIN_X_X_SAMPLES_PER_CROSS) + 1;

		// allocate the table (with the last value 0) 
		m_sin_x_x.reset(new double[m_sin_x_x_samples]);
		// store sin(x)/x * KaiserWeighting
		m_sin_x_x[0] = 1;
		double x, dx = 1.0 / SIN_X_X_SAMPLES_PER_CROSS;
		for (unsigned long k = 1; k < m_sin_x_x_samples; k++)
		{
			x = k * dx;
			m_sin_x_x[k] = sin(M_PI * x) / M_PI / x * kaiser_(x, m_kaiser_winlen, kaiser_parameter, kaiser_bessel_parameter);
		}

		return true;
	}
	// finds the (left) index in the RPM array acc.to the given time
	void getRpsIndex(double time, uint32_t &rps_index)
	{
		if (time < 0)
			rps_index = 0;
		else
		{
			rps_index = (uint32_t)(time / m_rpm_dt + 0.5);
			if (rps_index * m_rpm_dt > time)
				rps_index--;
			if (rps_index >= (uint32_t)m_rpm_data.size())
				rps_index = (uint32_t)m_rpm_data.size() - 1;
		}
	}
	double getNextRps(double time, uint32_t &rps_index, double &rps_time)
	{	// find the index to the left
		getRpsIndex(time, rps_index);
		rps_index++;//to the right (the next RPS)
		if (rps_index >= (uint32_t)m_rpm_data.size())
		{
			rps_index = (uint32_t)m_rpm_data.size() - 1;
			rps_time = time + 1;
		}
		else
			rps_time = rps_index * m_rpm_dt;
		// RPS from the RPM array
		return m_rpm_data[rps_index];
	}

	double getPrevRps(double time, uint32_t &rps_index, double &rps_time)
	{
		// find the index to the left (previuous RPS)
		getRpsIndex(time, rps_index);
		rps_time = rps_index * m_rpm_dt;
		// RPS from the RPM array	
		return m_rpm_data[rps_index];
	}
	// interpolate one sound sample at a given time acc.to the angle sample interval 
	// using the tabled Kaiser Window
	double getSample(double time, double rps)
	{
		// time_index: fractional index of desired sound sample, ul_time_index: lower integer index (time_index>=ul_time_index) 
		double time_index = time * m_param.samplerate, ul_time_index = (uint32_t)time_index;

		if (rps < m_rpm_min)
			rps = m_rpm_min;

		// window length is KAISER_WINLEN=2*KAISER_WINLEN_HALF+1 for pure interpolation
		uint32_t winlen_half = m_kaiser_winlen_half;

		// required Cut-Off Frequency for the Anti-Aliasing Filter 
		double fcut = m_cutoff_order * rps, winlen = m_kaiser_winlen, down_factor = 1;

		// realize cut-off frequency by stretching of the original impulse response (finer sample interval in the table) 
		if (2 * fcut < m_param.samplerate)				// Anti-Aliasing filtering required 
		{
			down_factor = 2 * fcut / m_param.samplerate;	// Down-sampling factor (<1)
			winlen_half = (uint32_t)ceil(winlen / down_factor / 2.0);	// increased half-window length
			if (winlen_half > KAISER_WINLEN_HALF_MAX)
				winlen_half = KAISER_WINLEN_HALF_MAX;
			winlen = 2 * winlen_half + 1;							// new increased window length
			down_factor = m_kaiser_winlen / winlen;			// realized donwsampling factor 
		}

		// time: absolute index for the first required sound sample for the interpolation/filtering
		uint32_t samples = 2 * winlen_half;// num. of used impulse response samples
		time = ul_time_index - winlen_half + 1;
		if (ul_time_index == time_index)
		{
			time--;
			samples++;								// one sample more (the impulse response center)
		}
		if (time < 0)
			time = 0;
		else if (time >= (uint32_t)m_signal_data.size())
			time = (uint32_t)m_signal_data.size() - 1;

		if (time + samples >= (uint32_t)m_signal_data.size())
			samples = (uint32_t)m_signal_data.size() - (uint32_t)time;
		double *data = m_signal_data.data() + (uint32_t)time;	// points to the first required sample in the sound array

		// time index rel.to desired time index
		double sum = 0; // time<time_index
		time -= time_index; // time in the impulse response which is centered around 0  
		time *= down_factor;// stretched time foe downsampling //double kkk[512];kkk[s]=getSin_X_X(time)*down_factor;double fb=1,tmp= cmppow(z2f(kkk,&fb,down_factor*1.1,samples-1,0));tmp=10*log10(tmp);
		for (uint32_t s = 0; s < samples; s++, time += down_factor) // smaller time interval for downsampling (down_factor<=1) 
			sum += data[s] * getSin_X_X(time);// interpolation/filtering with FIR
		sum *= down_factor;	// scaling for downsampling case

		return sum;
	}
	// interpolate an lowpass impulse response sample linearly from the table
	double getSin_X_X(double x)
	{
		if (x < 0)
			x = -x;

		// interpolate between known values from the table
		x *= SIN_X_X_SAMPLES_PER_CROSS;
		unsigned long ulx = (unsigned long)x;

		if (ulx >= m_sin_x_x_samples)
			return 0;
		else if (ulx == x)
			return m_sin_x_x[ulx];
		else
			return m_sin_x_x[ulx] + (m_sin_x_x[ulx + 1] - m_sin_x_x[ulx])*(x - ulx);
	}
private:
	bool Init();
	OrderParams m_param;
	double m_power_ref;
	vector<double> m_rpm_data;
	float m_rpm_rate;
	double m_rpm_scale;
	double m_rpm_min, m_rpm_max;
	double m_rpm_dt;
	uint32_t m_cycles_per_dft;
	double m_order_res;
	uint32_t m_fft_size;
	uint32_t m_dft_win_length;
	bool m_fft_flag;
	uint32_t m_order_channels;
	double m_delta_phi;
	double m_nyquist_order;
	double m_cutoff_order;
	unique_ptr<double[]> m_dft_window;
	unique_ptr<complex<double>[]> m_dft_data;
	unique_ptr<complex<double>[]> m_dft_modul;
	unique_ptr<complex<double>[]> m_dft_chirp;
	unique_ptr<double[]> m_sin_x_x;
	vector<double> m_signal_data;
	uint32_t m_kaiser_winlen;
	uint32_t m_kaiser_winlen_half;
	uint32_t m_sin_x_x_samples;
	uint32_t m_total_frame;
	unique_ptr<thread> m_calc_thread;
	vector<unique_ptr<double[]>> m_results;
	CRPMCalc m_rpm_calc;
	double m_min_power_ref;
	int m_nProgress;
};


bool COrderAnalysis::COrderAnalysisImpl::Init()
{
	if (m_param.dataType == 0)
		m_power_ref = MAX_24BIT * MAX_24BIT * pow(10.0, -m_param.range / 10.0);
	else
		m_power_ref = pow(10.0, -m_param.range / 10.0);
	m_min_power_ref = pow(10, MIN_DB / 10) / m_power_ref;
	m_order_channels = (uint32_t)((m_param.maxOrder - m_param.minOrder) / m_param.spacing) + 1;
	return true;
}


void COrderAnalysis::COrderAnalysisImpl::SetRPMData(const float* rpm, int count, float samplerate)
{
	m_rpm_rate = samplerate;
	m_rpm_data.clear();
	for (int i = 0; i != count; ++i)
		m_rpm_data.push_back(rpm[i] / 60.0);
	m_rpm_calc.Init(samplerate, rpm, count);
}

COrderAnalysis::COrderAnalysis(OrderParams params)
	: m_pImpl(new COrderAnalysisImpl(params))
{

}

void COrderAnalysis::SetRPMData(const float* rpm, int count, float samplerate)
{
	return m_pImpl->SetRPMData(rpm, count, samplerate);
}

bool COrderAnalysis::Calculate(const int* data, int samples)
{
	return m_pImpl->Calculate(data, samples);
}

bool COrderAnalysis::Calculate(const float* data, int samples)
{
	return m_pImpl->Calculate(data, samples);
}

int COrderAnalysis::GetProgress()
{
	return m_pImpl->GetProgress();
}

uint32_t COrderAnalysis::GetResultSize()
{
	return m_pImpl->GetResultSize();
}

bool COrderAnalysis::GetResultRPMInfo(uint32_t& startRPM, uint32_t& stopRPM, uint32_t& deltaRPM)
{
	return m_pImpl->GetResultRPMInfo(startRPM, stopRPM, deltaRPM);
}

bool COrderAnalysis::GetResult(double order, float* result)
{
	return m_pImpl->GetResult(order, result);
}

COrderAnalysis::~COrderAnalysis()
{
	delete m_pImpl;
}
