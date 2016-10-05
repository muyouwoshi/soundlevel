// Level.cpp : Defines the exported functions for the DLL application.
//

#define _USE_MATH_DEFINES
#include <math.h>
#include "Level.h"
#include <limits>
#include <vector>
#include <complex>
#include "math_ex.h"
#include "Tools.h"
using namespace std;

// index in FREQUENCIES
const int A_INDEX_1 = 0;
const int A_INDEX_2 = 1;
const int B_INDEX = 2;
const int C_INDEX_1 = 3;
const int C_INDEX_2 = 4;

// index in TIME_CONSTANTS
const int SLOW_INDEX = 0;
const int FAST_INDEX = 1;
const int IMPS_INDEX_1 = 2;
const int IMPS_INDEX_2 = 3;
const int LOUD_INDEX = 4;
const int HP20_INDEX = 5;	// spectral weighting 

const double k_l02_factor_l = 0.0061035156250; // (1.0/163.84) // Lin
const int MAX_ABC_SOS = 7; // + 2 for correction of bilinear transform (Number of Second-Order-Sections)

const int MAX_TMP_FOS = 6;	// Maximum Number of First Order Sections

class CLevelImpl {
public:
	CLevelImpl();
	CLevelImpl(LevelParam param);
	~CLevelImpl();
	int CalculateLevel(const int* buffer, uint32_t samples);
	void Init();
	uint32_t GetResult(double* result, double* pMax, double* pMin,
			double* pMean);
	const double* GetResult(uint32_t* count);
	LevelStatistic GetStatistic();
	LevelParam getM_param();
	void setM_param(LevelParam param);
	double getAbcMagnitude(double f, FreqWeighting type);
	void ResetResult();
//------------测试完了记得删掉--------
	double GetSamplerate();
	double GetRange();
	int GetWeighting();
	int GetAvarage();
	int GetTime_interval();
	//----------完毕-----------
private:
	void SetLevelDefault();
	int GetAbcCoeffs(double fw[][3], double fb[][3], int orders[], double fs,
			FreqWeighting type);
	void setAbcCoeffs(double *fw, double *fb, double k, int order, double fs);
	int setCorCoeffs(double fw[][3], double fb[][3], int section, double fs);
	double getAbcRawMagnitude(double f, FreqWeighting type);
	void getTmpCoeffs();
	void GetPower(double sample);
	double filterAbc(double s, bool no_abc_flag);
	void filterTmp(double p, TimeAvaraging tmp_type, int state_index = 0);
private:
	LevelParam m_param;
	double m_dbMax;
	double m_dbMin;
	int m_interval_count;
	int m_sample_interval;
	vector<double> m_result_data;
	double m_db_file_factor;
	double m_LMax;
	double m_PMax;
	double m_LMin;
	double m_PMin;
	double m_Pref;
	double m_powerDivider;
	double m_level_max;
	double m_level_mean;
	double m_sample_total;
	double m_current_sample;
	bool m_use_highpass;
	bool m_reset_flag;
	struct {
		double fw[MAX_ABC_SOS][3];
		double fb[MAX_ABC_SOS][3];
		double st[MAX_ABC_SOS][3];
		int sos;
		int orders[MAX_ABC_SOS];
	} m_abc_filter;
	struct {
		double fw[MAX_TMP_FOS][2];	// 5 Time Constants
		double fb[MAX_TMP_FOS][2];
		double st[MAX_TMP_FOS][2];
	} m_tmp_filter;
	double FREQUENCIES[5];
	double TIME_CONSTANTS[MAX_TMP_FOS];
};

//HLEVEL InitLevel(LevelParam param)
//{
//	CLevelImpl * pLevel = new CLevelImpl(param);
//	pLevel->Init();
//	return pLevel;
//}
//
//void UninitLevel(HLEVEL handle)
//{
//	if (handle)
//	{
//		delete static_cast<CLevelImpl*>(handle);
//	}
//}
//
//int LEVEL_API CalculateLevel(HLEVEL handle, const int* buffer, uint32_t samples)
//{
//	CLevelImpl* pLevel = static_cast<CLevelImpl*>(handle);
//	return pLevel->CalculateLevel(buffer, samples);
//}
//
//uint32_t GetLevelResult(HLEVEL handle, double* result, double* pMax, double* pMin, double* pMean)
//{
//	CLevelImpl* pLevel = static_cast<CLevelImpl*>(handle);
//	return pLevel->GetResult(result, pMax, pMin, pMean);
//}

double LEVEL_API GetAbcMagnitude(double f, FreqWeighting weighting) {
	CLevelImpl level;
	return level.getAbcMagnitude(f, weighting);
}

CLevelImpl::CLevelImpl() {
	SetLevelDefault();
}

CLevelImpl::CLevelImpl(LevelParam param) :
		m_param(param) {
	SetLevelDefault();

}

CLevelImpl::~CLevelImpl() {

}

int CLevelImpl::CalculateLevel(const int* buffer, uint32_t samples) {
	for (int k = 0; k != samples; ++k) {
		// one sample from the buffer
		double sample = buffer[k];

		// compute the power (spectral and time weighted). Result is in PEGEL.pcur 
		GetPower(sample);
		// Output interval
		if (++m_interval_count == m_sample_interval) {
			m_interval_count = 0;
			// Power scaling 
			sample = m_current_sample / m_powerDivider;

			// FLAT_AVERAGE and PEAK_AVERAGE
			if (m_reset_flag)
				m_current_sample = 0.0;

			// mean power over the wole analysis time range
			m_sample_total += sample;

			// Level
			if (sample < m_PMin)
				sample = m_LMin;
			else
				sample = 10 * log10(sample);

			// save the result in the output array
			m_result_data.push_back(sample);
			// Statistics
			if (sample > m_dbMax)
				m_dbMax = sample;

			if (sample < m_dbMin)
				m_dbMin = sample;
		}
	}
	return 0;
}

void CLevelImpl::SetLevelDefault() {
	m_dbMin = 0.0;
	m_dbMax = 0.0;
	m_interval_count = 0;
	m_db_file_factor = k_l02_factor_l;
	m_use_highpass = false;

	// spectral weighting filter design parameters
	// 20.598997, 107.65265, 737.86223, 12194.22, 158.48932
	FREQUENCIES[A_INDEX_1] = 107.65265;
	FREQUENCIES[A_INDEX_2] = 737.86223;
	FREQUENCIES[C_INDEX_1] = 20.598997;
	FREQUENCIES[C_INDEX_2] = 12194.220;
	FREQUENCIES[B_INDEX] = 158.48932;
	// time constants in seconds
	//   1.000, 0.125,    0.035,    1.500, 0.006
	//	   SLOW,  FAST, IMPULSE1, IMPULSE2,   EAR
	TIME_CONSTANTS[SLOW_INDEX] = 1.000;
	TIME_CONSTANTS[FAST_INDEX] = 0.125;
	TIME_CONSTANTS[IMPS_INDEX_1] = 0.035;
	TIME_CONSTANTS[IMPS_INDEX_2] = 1.500;
	TIME_CONSTANTS[LOUD_INDEX] = 0.006;
	m_result_data.clear();
}

void CLevelImpl::Init() {
	m_sample_total = 0.0;
	m_current_sample = 0.0;
	memset(&m_tmp_filter, 0, sizeof(m_tmp_filter));
	memset(&m_abc_filter, 0, sizeof(m_abc_filter));
	m_level_mean = m_level_max = 0.0;
	m_LMax = SHRT_MAX * m_db_file_factor;
	m_PMax = pow(10.0, m_LMax / 10.0);
	m_LMin = SHRT_MIN * m_db_file_factor;
	m_PMin = pow(10.0, m_LMin / 10.0);
	m_dbMin = m_LMax;
	m_dbMax = m_LMin;
	if (m_param.weighting == NoWeighting) {
		m_param.weighting = AWeighting;
		m_use_highpass = true;
	}
	m_sample_interval = static_cast<int>(m_param.samplerate
			* m_param.time_interval / 1000 + 0.5);
	m_Pref = 8388608.0 * 8388608.0 * pow(10.0, -m_param.range / 10.0);
	// Power Reference for Level calculation acc.to max. peak level 
	m_powerDivider = m_Pref;
	// Flat Averaging: Leq for one time interval is computed
	// therefore the integrated powers must be divided by the num of points used
	if (m_param.avarage == FlatAverage)
		m_powerDivider *= m_sample_interval;
	// reset the power to zero at the beginning of a new time interval
	m_reset_flag = (m_param.avarage == PeakAverage
			|| m_param.avarage == FlatAverage);
	m_abc_filter.sos = GetAbcCoeffs(m_abc_filter.fw, m_abc_filter.fb,
			m_abc_filter.orders, m_param.samplerate, m_param.weighting);
	getTmpCoeffs();
}

int CLevelImpl::GetAbcCoeffs(double fw[][3], double fb[][3], int orders[],
		double fs, FreqWeighting type) {
	double kappa = 2.0 * fs, k, a;
	int section = 0;
	// fixed parameters
	a = 2 * M_PI * FREQUENCIES[C_INDEX_1];	//20.598997;
	k = (a - kappa) / (a + kappa);
	orders[section] = 2;
	setAbcCoeffs(fw[section], fb[section], k, 2, fs);

	section++;
	a = 2 * M_PI * FREQUENCIES[C_INDEX_2];	//12194.220;
	k = (a - kappa) / (a + kappa);
	orders[section] = 2;
	setAbcCoeffs(fw[section], fb[section], k, 2, fs);

	if (type == AWeighting) {
		section++;
		a = 2 * M_PI * FREQUENCIES[A_INDEX_1];	//107.65265;
		k = (a - kappa) / (a + kappa);
		orders[section] = 1;
		setAbcCoeffs(fw[section], fb[section], k, 1, fs);

		section++;
		a = 2 * M_PI * FREQUENCIES[A_INDEX_2];	//737.86223;
		k = (a - kappa) / (a + kappa);
		orders[section] = 1;
		setAbcCoeffs(fw[section], fb[section], k, 1, fs);
	} else if (type == BWeighting) {
		section++;
		a = 2 * M_PI * FREQUENCIES[B_INDEX];	//158.48932;
		k = (a - kappa) / (a + kappa);
		orders[section] = 1;
		setAbcCoeffs(fw[section], fb[section], k, 1, fs);
	}

	// Correct differences at high frequencies from desired response
	section = setCorCoeffs(fw, fb, section, fs);

	return (section + 1);
}

void CLevelImpl::setAbcCoeffs(double *fw, double *fb, double k, int order,
		double fs) {
	double gain;

	if (order == 2) {
		fw[0] = 1.0;
		fw[1] = 0.0;
		fw[2] = -1.0;

		fb[0] = 1.0;
		fb[1] = 2 * k;
		fb[2] = k * k;
	} else {
		fw[0] = 1.0;
		fw[1] = -1.0;

		fb[0] = 1.0;
		fb[1] = k;
	}

	// set the gain
	gain = abs(z2f(fw, fb, 2000.0 / fs, order, order));
	for (int c = 0; c <= order; c++)
		fw[c] /= gain;
}

// unscaled spectral weighting filter magnitude response
double CLevelImpl::getAbcRawMagnitude(double f, FreqWeighting type) {
	double num, den;

	num = f * f;
	den = (f * f + FREQUENCIES[C_INDEX_1] * FREQUENCIES[C_INDEX_1])
			* (f * f + FREQUENCIES[C_INDEX_2] * FREQUENCIES[C_INDEX_2]);

	if (type == BWeighting) {
		num *= f;
		den *= sqrt(f * f + FREQUENCIES[B_INDEX] * FREQUENCIES[B_INDEX]);
	} else if (type == AWeighting) {
		num *= (f * f);
		den *= sqrt(
				(f * f + FREQUENCIES[A_INDEX_1] * FREQUENCIES[A_INDEX_1])
						* (f * f
								+ FREQUENCIES[A_INDEX_2]
										* FREQUENCIES[A_INDEX_2]));
	}

	return (num / den);
}

// spectral weighting filter magnitude response (0dB at 1 kHz)
double CLevelImpl::getAbcMagnitude(double f, FreqWeighting type) {
	if (type == NoWeighting)
		return 1;

	double m = getAbcRawMagnitude(f, type);
	m /= getAbcRawMagnitude(1000, type);

	return m;
}

int CLevelImpl::setCorCoeffs(double fw[][3], double fb[][3], int section,
		double fs) {
	double f1, m1, f2, m2, k;

	// Correction frequency
	f1 = 1000 * pow(10.0, 1.3); // max. DIN Frequency
	k = pow(10.0, 0.1);
	while (f1 > fs / 2.0) {
		f1 /= k;
	}

	// Desired magnitude response at frequency f1
	m1 = getAbcMagnitude(f1, m_param.weighting);
	for (signed char c = 0; c <= section; c++) {
		m1 /= abs(
				z2f(fw[c], fb[c], f1 * 2 / fs, m_abc_filter.orders[c],
						m_abc_filter.orders[c]));
	}
	// Desired magnitude response at frequency f2
	f2 = (f1 + fs / 2.0) / 2.0;
	m2 = getAbcMagnitude(f2, m_param.weighting);
	for (signed char c = 0; c <= section; c++) {
		m2 /= abs(
				z2f(fw[c], fb[c], f2 * 2 / fs, m_abc_filter.orders[c],
						m_abc_filter.orders[c]));
	}
	// parameterized highpass for correction
	section++;
	m_abc_filter.orders[section] = 1;
	getHighCoeffs(fw[section], fb[section], fs, sqrt(m1), f1, sqrt(m2), f2);
	m1 = abs(z2f(fw[section], fb[section], 2000. / fs, 1, 1));
	for (int c = 0; c <= 1; c++)
		fw[section][c] /= m1;

	section++;
	m_abc_filter.orders[section] = 1;
	for (signed char c = 0; c <= 1; c++) {
		fw[section][c] = fw[section - 1][c];
		fb[section][c] = fb[section - 1][c];
	}

	return section;
}

void CLevelImpl::getTmpCoeffs() {
	double fcut, a;
	//   1.000, 0.125,    0.035,    1.500, 0.006
	//	   SLOW,  FAST, IMPULSE1, IMPULSE2,   EAR

	// SLOW
	fcut = 1.0 / 2.0 / M_PI / TIME_CONSTANTS[SLOW_INDEX];
	GetLowPassCoeffs(m_tmp_filter.fw[SLOW_INDEX], m_tmp_filter.fb[SLOW_INDEX],
			m_param.samplerate, fcut);
	//	getImInLowPassCoeffs( PEGEL_TMP.fw[SLOW_INDEX], PEGEL_TMP.fb[SLOW_INDEX], PEGEL.fs, TIME_CONSTANTS[SLOW_INDEX] );

	// FAST
	fcut = 1.0 / 2.0 / M_PI / TIME_CONSTANTS[FAST_INDEX];
	GetLowPassCoeffs(m_tmp_filter.fw[FAST_INDEX], m_tmp_filter.fb[FAST_INDEX],
			m_param.samplerate, fcut);

	// IMPULSE
	fcut = 1.0 / 2.0 / M_PI / TIME_CONSTANTS[IMPS_INDEX_1];
	GetLowPassCoeffs(m_tmp_filter.fw[IMPS_INDEX_1],
			m_tmp_filter.fb[IMPS_INDEX_1], m_param.samplerate, fcut);

	// peak detector
	a = exp(-1.0 / m_param.samplerate / TIME_CONSTANTS[IMPS_INDEX_2]);
	m_tmp_filter.fw[IMPS_INDEX_2][0] = 1.0 - a; //PEGEL_TMP.fw[IMPS_INDEX_2][1] = 0.0;
	m_tmp_filter.fb[IMPS_INDEX_2][1] = a; // PEGEL_TMP.fb[IMPS_INDEX_2][0] = 1.0;

	// LOUDNESS
	fcut = 1.0 / 2.0 / M_PI / TIME_CONSTANTS[LOUD_INDEX];
	GetLowPassCoeffs(m_tmp_filter.fw[LOUD_INDEX], m_tmp_filter.fb[LOUD_INDEX],
			m_param.samplerate, fcut);

	// Highpass if no other spectral weighting
	fcut = 10.0;
	getHighCoeffs(m_tmp_filter.fw[HP20_INDEX], m_tmp_filter.fb[HP20_INDEX],
			m_param.samplerate, 1.0 / sqrt(2.0), fcut, 1.0,
			m_param.samplerate / 2.0, 0.0);
}

void CLevelImpl::GetPower(double sample) {
	// Spectral weighting
	sample = filterAbc(sample, m_use_highpass);

	// Squaring and Averaging
	filterTmp(sample * sample, m_param.avarage);
}

double CLevelImpl::filterAbc(double s, bool no_abc_flag) {
	if (no_abc_flag) //&&PEGEL.hp_flag == 1
	{
		s = filter(m_tmp_filter.fw[HP20_INDEX], m_tmp_filter.fb[HP20_INDEX], s,
				m_tmp_filter.st[HP20_INDEX]);
	} // ABC filter
	else
		for (int c = 0; c < m_abc_filter.sos; c++) {
			s = filter(m_abc_filter.fw[c], m_abc_filter.fb[c], s,
					m_abc_filter.st[c], m_abc_filter.orders[c],
					m_abc_filter.orders[c]);
		}

	return s;
}

void CLevelImpl::filterTmp(double p, TimeAvaraging time_type,
		int state_index /*= 0*/) {
	// time weighting mode
	if (time_type == SlowAverage)
		m_current_sample = filter(m_tmp_filter.fw[SLOW_INDEX],
				m_tmp_filter.fb[SLOW_INDEX], p, m_tmp_filter.st[state_index]);
	else if (time_type == FastAverage)
		m_current_sample = filter(m_tmp_filter.fw[FAST_INDEX],
				m_tmp_filter.fb[FAST_INDEX], p, m_tmp_filter.st[state_index]);
	else if (time_type == LoudAverage)
		m_current_sample = filter(m_tmp_filter.fw[LOUD_INDEX],
				m_tmp_filter.fb[LOUD_INDEX], p, m_tmp_filter.st[state_index]);
	else if (time_type == ImpsAverage) {
		p = filter(m_tmp_filter.fw[IMPS_INDEX_1], m_tmp_filter.fb[IMPS_INDEX_1],
				p, m_tmp_filter.st[state_index]);
		if (p >= m_current_sample)
			m_current_sample = p; // take the increased value
		else {
			m_current_sample *= m_tmp_filter.fb[IMPS_INDEX_2][1]; // slow decay
			if (p > m_current_sample) // current value>computed valued->use this one
				m_current_sample = p;
			// Long AND short time constant
			//p_out = PEGEL_TMP.fb[IMPS_INDEX_2][1] * p_out + PEGEL_TMP.fw[IMPS_INDEX_2][0] * p;
		}
	} else if (time_type == PeakAverage) {
		if (p > m_current_sample)	// max. instantaneuous power in the interval
			m_current_sample = p;
	} else if (time_type == FlatAverage) // Leq in the time interval
		m_current_sample += p;	// mean power in the time interval
	else
		m_current_sample = p;
}

uint32_t CLevelImpl::GetResult(double* result, double* pMax, double* pMin,
		double* pMean) {
	//update Statistics
	if (m_result_data.empty())
		return 0;
	m_level_max = m_dbMax;
	m_level_mean = m_sample_total / m_result_data.size();
	if (m_level_mean < m_PMin)
		m_level_mean = m_LMin;
	else
		m_level_mean = (10.0 * log10(m_level_mean));
	memcpy(result, m_result_data.data(), m_result_data.size() * sizeof(double));
	*pMax = m_dbMax;
	*pMin = m_dbMin;
	*pMean = m_level_mean;
	return static_cast<uint32_t>(m_result_data.size());
}

const double* CLevelImpl::GetResult(uint32_t* count) {
	*count = static_cast<uint32_t>(m_result_data.size());
	return m_result_data.data();
}

LevelStatistic CLevelImpl::GetStatistic() {
	LevelStatistic statistic = { 0 };
	if (!m_result_data.empty()) {
		double level_max = m_dbMax;
		double level_mean = m_sample_total / m_result_data.size();
		if (level_mean < m_PMin)
			level_mean = m_LMin;
		else
			level_mean = (10.0 * log10(level_mean));
		statistic.max = static_cast<float>(m_dbMax);
		statistic.mean = static_cast<float>(level_mean);
		statistic.min = static_cast<float>(m_dbMin);
	}
	return statistic;
}
LevelParam CLevelImpl::getM_param() {
	return m_param;
}
void CLevelImpl::setM_param(LevelParam param) {
	m_param = param;
}
//------------这里测完记得删掉-----------
double CLevelImpl::GetSamplerate(){
	return m_param.samplerate;
}
double CLevelImpl::GetRange(){
	return m_param.range;
}
int CLevelImpl::GetWeighting(){
	return m_param.weighting;
}
int CLevelImpl::GetAvarage(){
	return m_param.avarage;
}
int CLevelImpl::GetTime_interval(){
	return m_param.time_interval;
}
//------------完毕------------

void CLevelImpl::ResetResult() {
	m_result_data.clear();
}
CLevel::CLevel(LevelParam param) :
		m_pImpl(new CLevelImpl(param)) {
	static_cast<CLevelImpl*>(m_pImpl)->Init();
}

CLevel::~CLevel() {
	delete static_cast<CLevelImpl*>(m_pImpl);
}

int CLevel::Calculate(const int* data, int samples) {
	return static_cast<CLevelImpl*>(m_pImpl)->CalculateLevel(data, samples);
}

const double* CLevel::GetResult(uint32_t* count) {
	return static_cast<CLevelImpl*>(m_pImpl)->GetResult(count);
}

LevelStatistic CLevel::GetStatistic() {
	return static_cast<CLevelImpl*>(m_pImpl)->GetStatistic();
}
void CLevel::ResetResult() {
	return static_cast<CLevelImpl*>(m_pImpl)->ResetResult();
}
LevelParam CLevel::GetM_param() {
	return static_cast<CLevelImpl*>(m_pImpl)->getM_param();
}
void CLevel::SetM_param(LevelParam param) {
	return static_cast<CLevelImpl*>(m_pImpl)->setM_param(param);
}
double CLevel::GetSamplerate(){
	return static_cast<CLevelImpl*>(m_pImpl)->GetSamplerate();
}
double CLevel::GetRange(){
	return static_cast<CLevelImpl*>(m_pImpl)->GetRange();
}
int CLevel::GetWeighting(){
	return static_cast<CLevelImpl*>(m_pImpl)-> GetWeighting();
}
int CLevel::GetAvarage(){
	return static_cast<CLevelImpl*>(m_pImpl)->GetAvarage();
}
int CLevel::GetTime_interval(){
	return static_cast<CLevelImpl*>(m_pImpl)-> GetTime_interval();
}
