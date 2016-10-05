// FFT.cpp : Defines the exported functions for the DLL application.
//

#include "fft.h"
#include "math_ex.h"
#include "fft_tools.h"
#include <cassert>
#include <memory>
#include <vector>
#include <complex>

using namespace std;

const double MIN_LEVEL_DB = -300.0;	// minimaler Pegel enstprechend 0 in Datei
const double MAX_LEVEL_DB = 300.0;		// maximaler verarbeiteter Pegel ( 60000 )
const double FILE_FACTOR_DB = 100.0;
const int FFT_MAGNITUDE_SCALING = 0;		// magnitude scaling (FFT filters have maximum magnitude of 1)
const int FFT_POWER_SCALING = 1;		// power density scaling (sum of FFT filter powers is 1)
const int MAX_FFT_SAMPLES = 65536;		// maximum FFT-Size


class CFFTCalcImpl
{
public:
	CFFTCalcImpl(FFTParam param) : m_param(param)
	{
		DATA.Pref = 0;
		DATA.max_power = 0;
		DATA.min_power = 0;
		m_level_min = MAX_LEVEL_DB;
		m_level_max = 0;
		m_scale_type = FFT_MAGNITUDE_SCALING;
		m_scale_magnitude = 0.0;
		m_scale_power = 0;
		m_bandwidth = 0;
		m_fftlen = 0;
		m_fft_channels = 0;
		m_total_frame_count = 0;
		m_calc_size = 0;
		Init();
	}
	void Calculate(const int* data, int samples);
	void Calculate(const float* data, int samples);
	uint32_t GetResultInfo(uint32_t* channels)
	{
		*channels = m_fft_channels;
		if (m_frames.empty())
			return m_total_frame_count;
		return static_cast<uint32_t>(m_frames.size());
	}
	void GetMinMaxValue(float& min_value, float& max_value)
	{
		min_value = static_cast<float>(m_level_min);
		max_value = static_cast<float>(m_level_max);
	}
	bool GetResult(uint32_t index, float* result)
	{
		if (index >= static_cast<uint32_t>(m_frames.size()))
			return false;
		memcpy(result, m_frames[index].get(), m_fft_channels*sizeof(float));
		return true;
	}

	bool GetMeanResult(float* result)
	{
		if (m_total_count == 0)
			return false;
		for (int i = 0; i != m_fft_channels; ++i)
			result[i] = (float)(10 * log10(m_total_power[i] / m_total_count));
		return true;
	}

	void ResetMeanResult()
	{
		m_total_count = 0;
		memset(m_total_power.get(), 0, sizeof(double) * m_fft_channels);
	}

	//function for tonality
	double GetMinDeltaL(double cbw)
	{
		double ebw = m_bandwidth * m_scale_magnitude / m_scale_power*m_param.winlen / m_fftlen;
		return 10.0 * log10(ebw / cbw);
	}
	double GetDataPref()
	{
		return DATA.Pref;
	}
	double GetScalePower()
	{
		return m_scale_power;
	}
	double GetScaleMagnitude()
	{
		return m_scale_magnitude;
	}
	void ClearResult(){
		m_frames.clear();
	}
	void Reset();
	void SetFFTParam(FFTParam param){
		m_param=param;
	}
	FFTParam GetFFTParam(){
		return m_param;
	}
private:
	void Init()
	{
		// Level definition
		DATA.min_power = pow(10.0, MIN_LEVEL_DB / 10.0);
		DATA.max_power = pow(10.0, MAX_LEVEL_DB / 10.0);
		m_level_min = MAX_LEVEL_DB;
		m_level_max = MIN_LEVEL_DB;
		m_total_count = 0;
		switch (m_param.sample_type)
		{
		case 0:
			DATA.Pref = 8388608.0 * 8388608.0 * pow(10.0, -m_param.range / 10.0);
			break;
		case 1:
			DATA.Pref = pow(10.0, -m_param.range / 10.0);
			break;
		default:
			DATA.Pref = 0.0;
		}

		initFFTSpectrogram();
	}
	void allocateArrays()
	{
		DATA.power.assign(m_fft_channels * 2, 0);
		DATA.window.assign(m_param.winlen, 0);
		DATA.X.assign(m_fftlen, complex<double>());
		m_calc_data.reset(new int[m_param.winshift + m_param.winlen]);
		m_total_power.reset(new double[m_fft_channels]);
	}
	void initFFTSpectrogram()
	{
//		m_offset = -MIN_LEVEL_DB;
//		m_factor = FILE_FACTOR_DB;

		// Init. of max. file value evaluation

		// scale t a correct tone power
		m_scale_type = FFT_MAGNITUDE_SCALING;

		// window shift between adjacent sound segments
		if (m_param.winshift < 1)
			m_param.winshift = 1;


		// window length
		if (m_param.winlen < 4)
			m_param.winlen = 4;
		if (m_param.winshift > m_param.winlen)
			m_param.winshift = m_param.winlen;
		m_bandwidth = m_param.samplerate / m_param.winlen;
		m_fftlen = m_param.winlen;
		m_fftlen = static_cast<uint32_t>(pow(2.0, nextpow2(m_fftlen)));
		if (m_fftlen > MAX_FFT_SAMPLES)
			m_fftlen = MAX_FFT_SAMPLES;
		m_fft_channels = m_fftlen / 2 + 1;
		allocateArrays();
		m_param.winpar = getWindow(DATA.window.data(), m_param.winlen, m_param.type, m_param.winpar);
		getFFTScale(DATA.window.data(), m_param.winlen, m_fftlen, m_scale_magnitude, m_scale_power);
	}

	//! compute 2 FFT power spectra of two (overlapping) windowed sound sequences
	void getPowerSpectra()
	{
		// Start sample indices for two adjacent (overlapping) sound segments
		uint32_t k;

		// put the first segment (given in p_Data1) into the real part of DATA.X
		// p_Data1[0..winlen-1]	is already filled with sound data
		if (m_param.sample_type == 0)
		{
			for (k = 0; k < m_param.winlen; k++)
				DATA.X[k].real(m_calc_data[k] * DATA.window[k]);

			// put the 2.segment into the imag. part of DATA.X
			for (k = 0; k < m_param.winlen; k++)
				DATA.X[k].imag(m_calc_data[k + m_param.winshift] * DATA.window[k]);
		}
		else
		{
			const float * calc_data = reinterpret_cast<float*>(m_calc_data.get());
			for (k = 0; k < m_param.winlen; k++)
				DATA.X[k].real(calc_data[k] * DATA.window[k]);

			// put the 2.segment into the imag. part of DATA.X
			for (k = 0; k < m_param.winlen; k++)
				DATA.X[k].imag(calc_data[k + m_param.winshift] * DATA.window[k]);
		}
		// pad complex array with zeros if desired
		for (k = m_param.winlen; k < m_fftlen; k++)
		{
			DATA.X[k].real(0);
			DATA.X[k].imag(0);
		}

		// compute two power spectra
		doublefft(m_fftlen, DATA.X.data(), DATA.power.data());	//doublefft2( N1, X, DATA.power );
	}
	/*! compute levels from powers and transform into file values (unsigned short data)*/
	void getPowerLevels(int fft_blocks)
	{
		double L, P;
		for (int i = 0; i != fft_blocks; ++i)
		{
			m_frames.emplace_back(new float[m_fft_channels]);
			m_total_count++;
			for (uint32_t j = 0; j != m_fft_channels; ++j)
			{
				P = DATA.power[j+i*m_fft_channels];
				m_total_power[j] += P;
				if (P > DATA.min_power && P < DATA.max_power)
				{
					// sound pressure level, Power P is already calibrated
					L = 10.0 * log10(P);

					// find out min. Level
					if (L<m_level_min)
						m_level_min = L;

					// level as positive 2 byte value
//					file_val = (uint16_t)((L - MIN_LEVEL_DB) * FILE_FACTOR_DB + 0.5);

					// find out max. value in the file (not max.level)
					if (L > m_level_max)
						m_level_max = L;

				}
				else if (P >= DATA.max_power)// Overload
				{
//					file_val = (uint16_t)((MAX_LEVEL_DB - MIN_LEVEL_DB) * FILE_FACTOR_DB);
					m_level_max = MAX_LEVEL_DB;
					L = MAX_LEVEL_DB;
				}
				else // min. file value
				{
//					file_val = 0;
					m_level_min = MIN_LEVEL_DB;
					L = MIN_LEVEL_DB;
				}
				m_frames.back()[j] = static_cast<float>(L);
			}
		}
	}
private:
	struct
	{
		vector<double> power;				//!< FFT intensities
		vector<double> window;				//!< FFT window data(HAMMING, HANNING, ...)
		vector<complex<double>> X;					//!< complex array for FFT algorithm
		double Pref;				//!<  reference power for calibrated level computation
		double min_power;			//!<  min.power limit
		double max_power;			//!<  max.power limit
	} DATA;
	FFTParam m_param;
	double m_level_min;
//	double m_offset = -MIN_LEVEL_DB;
//	double m_factor = FILE_FACTOR_DB;
	double m_level_max;
	int m_scale_type;
	double m_scale_magnitude;
	double m_scale_power;
	double m_bandwidth;
	uint32_t m_fftlen;
	int m_fft_channels;
	int m_total_frame_count;
	unique_ptr<int[]> m_calc_data;
	uint32_t m_calc_size;
	vector<unique_ptr<float[]>> m_frames;
	unique_ptr<double[]> m_total_power;
	uint32_t m_total_count;
};

void CFFTCalcImpl::Calculate(const int* data, int samples)
{
	double scale = m_scale_magnitude / DATA.Pref;	// magnitude scaling
	const int* data_cp = data;
	uint32_t data_left = static_cast<uint32_t>(samples);
	uint32_t total = m_param.winlen + m_param.winshift;
	while (data_left > 0)
	{
		uint32_t maxCopy = min(total - m_calc_size, data_left);
		memcpy(m_calc_data.get() + m_calc_size, data_cp, maxCopy*sizeof(int));
		m_calc_size += maxCopy;
		data_left -= maxCopy;
		data_cp += maxCopy;
		if (m_calc_size == total)
		{
			getPowerSpectra();
			// scale and calibrate the powers of both spectra
			multiplyArray(DATA.power.data(), 2 * m_fft_channels, scale);

			// transform physical levels to unsigned short file values
			getPowerLevels(2);
			memmove(m_calc_data.get(), m_calc_data.get() + m_param.winshift * 2, (m_param.winlen - m_param.winshift)*sizeof(int));
			m_calc_size = m_param.winlen - m_param.winshift;
		}
	}
}

void CFFTCalcImpl::Calculate(const float* data, int samples)
{
	Calculate(reinterpret_cast<const int*>(data), samples);
}


void CFFTCalcImpl::Reset()
{
	m_calc_size = 0;
	m_level_min = MAX_LEVEL_DB;
	m_level_max = MIN_LEVEL_DB;
	m_frames.clear();
}

CFFTCalc::CFFTCalc(FFTParam param) : m_pImpl(new CFFTCalcImpl(param))
{

}

CFFTCalc::~CFFTCalc()
{
	delete static_cast<CFFTCalcImpl*>(m_pImpl);
}

void CFFTCalc::Calculate(const int* data, int samples)
{
	return static_cast<CFFTCalcImpl*>(m_pImpl)->Calculate(data, samples);
}

void CFFTCalc::Calculate(const float* data, int samples)
{
	return static_cast<CFFTCalcImpl*>(m_pImpl)->Calculate(data, samples);
}

uint32_t CFFTCalc::GetResultInfo(uint32_t* channels)
{
	return static_cast<CFFTCalcImpl*>(m_pImpl)->GetResultInfo(channels);
}

bool CFFTCalc::GetResult(uint32_t index, float* result)
{
	return static_cast<CFFTCalcImpl*>(m_pImpl)->GetResult(index, result);
}

bool CFFTCalc::GetMeanResult(float* result)
{
	return static_cast<CFFTCalcImpl*>(m_pImpl)->GetMeanResult(result);
}

void CFFTCalc::ResetMeanResult()
{
	return static_cast<CFFTCalcImpl*>(m_pImpl)->ResetMeanResult();
}

void CFFTCalc::GetMinMaxValue(float& min_value, float& max_value)
{
	return static_cast<CFFTCalcImpl*>(m_pImpl)->GetMinMaxValue(min_value, max_value);
}
void CFFTCalc::ResultClear()
{
	return static_cast<CFFTCalcImpl*>(m_pImpl)->ClearResult();
}

void CFFTCalc::Reset()
{
	return static_cast<CFFTCalcImpl*>(m_pImpl)->Reset();
}

void CFFTCalc::SetFFTParam(FFTParam param) {
	return static_cast<CFFTCalcImpl*>(m_pImpl)->SetFFTParam(param);
}
FFTParam CFFTCalc::GetFFTParam(){
	return static_cast<CFFTCalcImpl*>(m_pImpl)->GetFFTParam();
}

