// ThirdOctave.cpp : Defines the exported functions for the DLL application.
//
#define _USE_MATH_DEFINES
#include <math.h>
#include <limits.h>
#include <memory>
#include <vector>
#include <functional>
#include <numeric>
#include <algorithm>
using namespace std;
#include "math_ex.h"
#include "Tools.h"
#include "Level.h"
#include "ThirdOctave.h"
#include "AIInterface.h"

#ifdef __cplusplus
extern "C" {
#endif

#define JNI_DEBUG

#ifdef JNI_DEBUG

#ifndef LOG_TAG
#define LOG_TAG "JNI_DEBUG"
#endif

#define LOGE(msg) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, msg)
#define LOGI(msg) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, msg)
#define LOGD(msg) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, msg)

#endif

const int MAX_NUM_OCTAVE_CHANNELS = 6;
const int MAX_NUM_OCTAVES = 10;
const int MAX_NUM_SOS = 5;

const double sample_rate = 48000.0;
const double k_l02_factor_l = 0.0061035156250; // (1.0/163.84) // Lin

////////////////////////////////////////////////////////////////////////
// Third-Octave filter definitions for Loudness Calculation

// Upper Third Octave
static double LOUD_UPP_FW[MAX_NUM_SOS][3] = { { 1.0, 0.0, -1.0 }, { 1.0, -2.0,
		1.0 }, { 1.0, 0.0, -1.0 } };

static double LOUD_UPP_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-0.17355002123485, 0.91692286592358 }, { 1.00000000000000,
		0.12270553014000, 0.82491659656000 }, { 1.00000000000000,
		0.45000116635667, 0.91703886259629 } };

// Middle Third-Octave
static double LOUD_MID_FW[MAX_NUM_SOS][3] = { { 1.0, 0.0, -1.0 }, { 1.0, -2.0,
		1.0 }, { 1.0, 0.0, -1.0 } };

static double LOUD_MID_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-0.71802368431603, 0.93147299119751 }, { 1.00000000000000,
		-0.47449299340000, 0.85189254000000 }, { 1.00000000000000,
		-0.26574408703390, 0.92248247348229 } };

// Lower Third-Octave
static double LOUD_LOW_FW[MAX_NUM_SOS][3] = { { 1.00000000000000,
		0.00000000000000, -1.00000000000000 }, { 1.00000000000000,
		-2.00000000000000, 1.00000000000000 }, { 1.00000000000000,
		0.00000000000000, -1.00000000000000 }, { 1.00000000000000,
		1.00000000000000, 1.00000000000000 }, { 1.00000000000000,
		0.50000000000000, 1.00000000000000 }

};

static double LOUD_LOW_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-1.14327200399688, 0.93362740425032 }, { 1.00000000000000,
		-0.90064138225000, 0.85552003592000 }, { 1.00000000000000,
		-0.78518891050289, 0.93113658243345 }, { 1.00000000000000,
		0.00000000000000, 0.00000000000000 }, { 1.00000000000000,
		0.00000000000000, 0.00000000000000 } };

// Downsampling Cauer Lowpass
static double LOUD_CUT_FW[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-0.19523081116000, 1.00000000000000 }, { 1.00000000000000,
		-0.03420382369200, 1.00000000000000 }, { 0.50000000000000,
		0.92495709992000, 0.50000000000000 }, { 0.50000000000000,
		0.52377309881000, 0.50000000000000 }, { 1.00000000000000,
		0.35036019037000, 1.00000000000000 }

};

static double LOUD_CUT_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-0.91667646793000, 0.93496381615000 }, { 1.00000000000000,
		-0.94115105526000, 0.80298033185000 }, { 1.00000000000000,
		-1.23837731560000, 0.40593818780000 }, { 1.00000000000000,
		-1.14927474092000, 0.50747287882000 }, { 1.00000000000000,
		-1.03009760236000, 0.65624934886000 } };

////////////////////////////////////////////////////////////////////
// IEC Main Filters
static double IEC_MAIN_UPP_FW[MAX_NUM_SOS][3] = { { 1.00000000000000,
		0.00000000000000, -1.00000000000000 }, { 1.00000000000000,
		0.00000000000000, -1.00000000000000 }, { 1.00000000000000,
		0.00000000000000, -1.00000000000000 }, { 1.00000000000000,
		-1.00000000000000, 0.50000000000000 } };

static double IEC_MAIN_UPP_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		0.90778185241000, 0.71634300680000 }, { 1.00000000000000,
		1.27236892190000, 0.87031236577000 }, { 1.00000000000000,
		0.61372947475000, 0.83626838636000 }, { 1.00000000000000,
		-1.06403990580000, 0.37934758528000 } };

static double IEC_MAIN_MID_FW[MAX_NUM_SOS][3] = { { 1.0, 0.0, -1.0 }, { 1.0,
		0.0, -1.0 }, { 1.0, 0.0, -1.0 } };

static double IEC_MAIN_MID_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		0.18538141157000, 0.76857577132000 }, { 1.00000000000000,
		0.49360321522000, 0.88208941252000 }, { 1.00000000000000,
		-0.10905216113000, 0.87826629824000 } };

static double IEC_MAIN_LOW_FW[MAX_NUM_SOS][3] = { { 1.0, 0.0, -1.0 }, { 1.0,
		0.0, -1.0 }, { 1.0, 0.0, -1.0 } };
static double IEC_MAIN_LOW_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-0.22086119288000, 0.90106346843000 }, { 1.00000000000000,
		-0.69054026919000, 0.90686649739000 }, { 1.00000000000000,
		-0.44091017111000, 0.81400931071000 } };

////////////////////////////////////////////////////////////////////
// IEC Side Band Filters
static double IEC_SIDE_UPP_FW[MAX_NUM_SOS][3] = { { 0.50000000000000,
		0.99134650012133, 0.50000000000000 }, { 0.50000000000000,
		-0.97288555296348, 0.50000000000000 }, { 1.00000000000000,
		0.00000000000000, -1.00000000000000 } };

static double IEC_SIDE_UPP_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		0.83765727905872, 0.88485198182701 }, { 1.00000000000000,
		0.18293683649797, 0.87288754915575 }, { 1.00000000000000,
		0.49433829624050, 0.76449449386221 } };

static double IEC_SIDE_MID_FW[MAX_NUM_SOS][3] = { { 0.50000000000000,
		0.96921931378470, 0.50000000000000 }, { 0.50000000000000,
		-0.97864030539380, 0.50000000000000 }, { 1.00000000000000,
		0.00000000000000, -1.00000000000000 } };

static double IEC_SIDE_MID_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		0.10127324768453, 0.90000143398255 }, { 1.00000000000000,
		-0.44520992772822, 0.90251101832215 }, { 1.00000000000000,
		-0.16683036350138, 0.80796221623811 } };

static double IEC_SIDE_LOW_FW[MAX_NUM_SOS][3] = { { 0.50000000000000,
		0.92326104679693, 0.50000000000000 }, { 0.50000000000000,
		-0.98425525835061, 0.50000000000000 }, { 1.00000000000000,
		0.00000000000000, -0.50000000000000 }

};

static double IEC_SIDE_LOW_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-0.52258381397244, 0.91709893561150 }, { 1.00000000000000,
		-0.93093214101821, 0.92430634966231 }, { 1.00000000000000,
		-0.70711840560096, 0.84539692680135 } };

// Downsampling Lowpass
static double CUT_FW[MAX_NUM_SOS][3] = {

{ 1.00000000000000, 0.36653609240000, 1.00000000000000 }, { 0.50000000000000,
		0.25684537610000, 0.50000000000000 }, { 0.50000000000000,
		0.42257927760000, 0.50000000000000 }, { 0.50000000000000,
		0.69042397820000, 0.49999999990000 }, { 0.50000000000000,
		0.95494807410000, 0.49999999990000 }, };

static double CUT_FB[MAX_NUM_SOS][3] = { { 1.00000000000000, -0.33188707260000,
		0.93123230890000 }, { 1.00000000000000, -0.41036081170000,
		0.78692318630000 }, { 1.00000000000000, -0.57885766620000,
		0.61241790940000 }, { 1.00000000000000, -0.80160193220000,
		0.41712176260000 }, { 1.00000000000000, -0.97575558790000,
		0.27233974820000 } };

////////////////////////////////////////////////////////////////////////
// ANSI Main Filters
static double ANSI_MAIN_UPP_FW[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-1.90266176987507, 0.99999999996865 }, { 1.00000000000000,
		1.94893552351350, 1.00000000000000 }, { 1.00000000000000,
		1.99046452059232, 1.00000000000000 }, { 1.00000000000000,
		-1.52431735801189, 1.00000000000000 }

};

static double ANSI_MAIN_UPP_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		1.07752822660000, 0.78330402727000 }, { 1.00000000000000,
		0.58441238624000, 0.89080759986000 }, { 1.00000000000000,
		1.32732853600000, 0.91608898707000 }, { 1.00000000000000,
		0.77501756304000, 0.75916467061000 } };

static double ANSI_MAIN_MID_FW[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-1.73754312718643, 0.99999999998360 }, { 1.00000000000000,
		1.96530872873275, 0.99999999994815 }, { 1.00000000000000,
		1.82331481470218, 0.99999999997393 }, { 1.00000000000000,
		-1.94752174329527, 0.99999999993050 } };

static double ANSI_MAIN_MID_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-0.14561084490000, 0.92001109734000 }, { 1.00000000000000,
		0.05225658950200, 0.81343243637000 }, { 1.00000000000000,
		0.53396703307000, 0.92282244538000 }, { 1.00000000000000,
		0.32407577051000, 0.81608233393000 } };

static double ANSI_MAIN_LOW_FW[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-1.83274325925391, 1.00000000001121 }, { 1.00000000000000,
		-1.96641312637495, 1.00000000002920 }, { 1.00000000000000,
		1.57889174453410, 1.00000000001812 }, { 1.00000000000000,
		1.91071086140191, 1.00000000004576 } };

static double ANSI_MAIN_LOW_FB[MAX_NUM_SOS][3] = { { 1.00000000000000,
		-0.72748238159000, 0.93878962788000 }, { 1.00000000000000,
		-0.55526976819000, 0.85253157304000 }, { 1.00000000000000,
		-0.19558562808000, 0.93445011263000 }, { 1.00000000000000,
		-0.34117115263000, 0.84841774831000 } };

////////////////////////////////////////////////////////////////////
// Delays for all filters of the Impulse Response Maxima in milli sec.

static double IEC_MAIN_DELAY_MS[MAX_NUM_OCTAVES * 3] = { 0.0000, 0.0625, 0.1250,

0.4375, 0.4792, 0.6042, 1.1875, 1.2708, 1.5208, 2.6042, 2.9375, 3.2708, 5.7708,
		6.1042, 7.1042, 11.7708, 12.4375, 14.4375, 23.7708, 25.1042, 29.1042,
		47.7708, 50.4375, 58.4375, 95.7708, 101.1042, 117.1042, 191.7708,
		202.4375, 234.4375 };

static double IEC_SIDE_DELAY_MS[MAX_NUM_OCTAVES * 3] = { 0.0000, 0.0625, 0.1250,

0.3958, 0.5208, 0.6458, 1.1458, 1.3958, 1.6458, 2.7292, 3.0625, 3.5625, 5.7292,
		6.3958, 7.7292, 11.7292, 13.7292, 15.7292, 23.7292, 27.7292, 31.7292,
		47.7292, 55.7292, 63.7292, 95.7292, 111.7292, 127.7292, 191.7292,
		223.7292, 255.7292 };

static double IEC_MAIN_SIDE_DELAY_MS[MAX_NUM_OCTAVES * 6] = { 0.0000, 0.0417,
		0.0625, 0.1042, 0.1250, 0.1667, 0.4375, 0.4375, 0.4792, 0.5625, 0.6042,
		0.6875, 1.1875, 1.1875, 1.2708, 1.4375, 1.5208, 1.6875, 2.6042, 2.7708,
		2.9375, 3.1042, 3.2708, 3.6042, 5.7708, 5.7708, 6.1042, 6.4375, 7.1042,
		7.7708, 11.7708, 11.7708, 12.4375, 13.7708, 14.4375, 15.7708, 23.7708,
		23.7708, 25.1042, 27.7708, 29.1042, 31.7708, 47.7708, 47.7708, 50.4375,
		55.7708, 58.4375, 63.7708, 95.7708, 95.7708, 101.1042, 111.7708,
		117.1042, 127.7708, 191.7708, 191.7708, 202.4375, 223.7708, 234.4375,
		255.7708 };

static double ANSI_MAIN_DELAY_MS[MAX_NUM_OCTAVES * 3] = { 0.0000, 0.0833,
		0.1875, 0.4792, 0.6042, 0.7708, 1.4375, 1.6042, 1.9375, 3.1875, 3.6875,
		4.3542, 6.6875, 7.6875, 9.0208, 14.3542, 15.6875, 18.3542, 29.0208,
		31.6875, 37.0208, 58.3542, 63.6875, 74.3542, 117.0208, 127.6875,
		149.0208, 234.3542, 255.6875, 298.3542 };

/*! Third-octave filter
 */
typedef struct {
	int sos;					//!< num so-sections
	int fw_orders[MAX_NUM_SOS]; //!< orders of the filter sections (1 or 2)
	int fb_orders[MAX_NUM_SOS];
	double *fw[MAX_NUM_SOS]; //!< pointer to coefficients in static double arrays
	double *fb[MAX_NUM_SOS];
	double states[MAX_NUM_OCTAVES][MAX_NUM_SOS][3];	//!< states for this filter for all octaves and sections

	// the same filter coefficients are used in every octave
	int dindex_2;	//!< doubled DIN index for filters (f=1000*2^(dindex_2/6))
	double f_fnyq;//!< center frequency rel.to half the samplerate (nyquistrate)
	double gain;					//!< gain at center frequency
} toct_struct;

//! struct for second-order filter
typedef struct {
	int type;
	int fw_order;
	int fb_order;
	double fw[3];
	double fb[3];
	double states[3];
	double f_fnyq;
	double gain;
} sos_struct;

class CThirdOctaveImpl {
public:
	CThirdOctaveImpl(ThirdOctaveParam parma) :
			m_param(parma) {

	}
	~CThirdOctaveImpl() {

	}
	int Init();
	int Calculate(const int* buffer, uint32_t samples);
	bool InitAiCalculate(function<void(const double*)> GetFrameLevels) {
		m_get_frame_levels = GetFrameLevels;
		return true;
	}
	void InitResultFunc(std::function<size_t()> GetResultCount,
			std::function<float*(size_t index)> GetResultPointer,
			std::function<int()> GetChannelCount) {
		m_get_result_count = GetResultCount;
		m_get_result_pointer = GetResultPointer;
		m_get_channel_count = GetChannelCount;
	}
	uint32_t GetResultInfo(uint32_t* channels) {
		*channels = m_total_channels;
		return static_cast<uint32_t>(m_result.size());
	}
	const float* GetResult(uint32_t index) {
		if (index >= m_result.size())
			return NULL;
		return m_result[index].get();
	}
	const float* GetMeanResult(uint32_t index) {
		if (!m_mean_result)
			m_mean_result.reset(new float[m_total_channels]);
		const float* data = GetResult(index);
		double tmp;
		for (int i = 0; i != m_total_channels; ++i) {
			tmp = data[i] + m_db_weighting[i];
			m_mean_result[i] = static_cast<float>(pow(10.0, tmp / 10.0));
		}
		return m_mean_result.get();
	}
	void GetFinalMean(const float* data, float* output, uint32_t count) {
		for (uint32_t i = 0; i != count; ++i) {
			output[i] = static_cast<float>(10.0 * log10(data[i]));
		}
	}
	int GetCalculateChannelCount() {
		return m_total_channels;
	}
	int GetCalcResultCount(uint32_t* count, uint32_t* frame_size) {
		*count = static_cast<uint32_t>(m_result.size());
		*frame_size = m_total_channels;
		return 0;
	}
	int GetCalcResult(uint32_t index, float* frame) {
		if (index >= m_result.size())
			return -1;
		memcpy(frame, m_result[index].get(), m_total_channels * sizeof(short));
		return 0;
	}
	int GetMeanMaxResultCount(uint32_t* count_time, uint32_t* count_freq) {
		*count_time = static_cast<uint32_t>(m_result.size());
		*count_freq = m_total_channels;
		return 0;
	}
	int GetMeanMaxResult(bool bMean, FreqWeighting weighting, double* data_time,
			double* data_freq);
	double getLevel(double p_pref);
	const double* GetToctFrame() {
		return m_frame;
	}
	void ResetResult() {
		m_result.clear();
	}
	void SetWeighting(FreqWeighting weighting);
	ThirdOctaveParam GetM_param() {
		return m_param;
	}
	void SetM_param(ThirdOctaveParam param) {
		m_param = param;
	}
private:
	void getDIndex();
	void initToctFilters();
	void copyToctCoeffs(toct_struct *toct, double fw[][3], double fb[][3]);
	// Averaging filters (envelope)
	void makeLowPasses(int channel);
	double setViperLowPass(sos_struct *sos, int oct, double cf_fnyq);
	void setCawToctLowPass(sos_struct *sos, int oct, double tau,
			ThirdOctaveAvarageMethod tau_method);
	void setCawToctLowPass(sos_struct *sos, int oct, double tau,
			ThirdOctaveAvarageMethod tau_method, double cf_fnyq);
	void setCawLoudLowPass(sos_struct *sos, int oct);
	void setFrameDelays();
	int getFrame(double sample, double *frame);
	void getFrameLevels(const double *frame);
	void UpdateFrameResult(const float* frame);
	double filterOctave(double s, int octave, double *frame);
	double filterCascade(double s, toct_struct *toct, sos_struct *pow, int oct);
	int GetTotalChannels() {
		return m_total_channels;
	}
private:
	ThirdOctaveParam m_param;
	int m_num_octaves;
	int m_dindex_2;
	int m_oct_channels;
	int m_sample_interval;
	int m_total_channels;
	int m_current_sample;
	FreqWeighting m_current_weighting;
	double m_db_weighting[MAX_NUM_OCTAVE_CHANNELS * MAX_NUM_OCTAVES];
	double m_LMax;
	double m_PMax;
	double m_LMin;
	double m_PMin;
	double m_Pref;
	double m_fs_out;
	double TOC_values[MAX_NUM_OCTAVES * MAX_NUM_OCTAVE_CHANNELS];
	double *frame_delays_pointer;
	double m_tocts_max;
	double m_tocts_min;
	toct_struct TOCT_FILTERS[MAX_NUM_OCTAVE_CHANNELS];// up to 6 filters per octave
	toct_struct TOCT_DOWN;					// half-rate downsampling lowpass
	sos_struct TOCT_POWERS[MAX_NUM_OCTAVE_CHANNELS][MAX_NUM_OCTAVES];// averaging lowpasses for all filters
	short frame_delays[MAX_NUM_OCTAVES * MAX_NUM_OCTAVE_CHANNELS];// filter delays
	double m_frame[MAX_NUM_OCTAVES * MAX_NUM_OCTAVE_CHANNELS];
	bool octave_flag[MAX_NUM_OCTAVES];
	vector<unique_ptr<float[]>> m_result;
	unique_ptr<float[]> m_mean_result;
	function<void(const double*)> m_get_frame_levels;
	function<size_t()> m_get_result_count;
	function<float*(size_t index)> m_get_result_pointer;
	function<int()> m_get_channel_count;
};

int CThirdOctaveImpl::Init() {
	memset(TOC_values, 0, sizeof(TOC_values));
	memset(TOCT_FILTERS, 0, sizeof(TOCT_FILTERS));
	memset(&TOCT_DOWN, 0, sizeof(TOCT_DOWN));
	memset(TOCT_POWERS, 0, sizeof(TOCT_POWERS));
	memset(frame_delays, 0, sizeof(frame_delays));
	memset(m_frame, 0, sizeof(m_frame));
	for (int i = 0; i != MAX_NUM_OCTAVES; ++i)
		octave_flag[i] = true;
	if (m_param.avarage == SlowAverage)
		m_param.time_constant = 1.0;
	else if (m_param.avarage == FastAverage)
		m_param.time_constant = 0.125;
	m_num_octaves = 10;
	m_LMax = SHRT_MAX * k_l02_factor_l;
	m_PMax = pow(10.0, m_LMax / 10.0);
	m_LMin = SHRT_MIN * k_l02_factor_l;
	m_PMin = pow(10.0, m_LMin / 10.0);
	m_Pref = 8388608.0 * 8388608.0 * pow(10.0, -m_param.range / 10.0);
	frame_delays_pointer = IEC_MAIN_DELAY_MS;
	m_fs_out = 500;	// (500 Hz fixed for loudness calculation)
	if (m_param.time_interval)
		m_fs_out = 1000.0 / m_param.time_interval;
	initToctFilters();
	m_total_channels = m_oct_channels * m_num_octaves;
	setFrameDelays();
	m_sample_interval = static_cast<int>(sample_rate * m_param.time_interval
			/ 1000 + 0.5);
	m_tocts_max = m_LMin;
	m_tocts_min = m_LMax;
	m_current_sample = 0;
	m_get_frame_levels = [this](const double* frame) {getFrameLevels(frame);};
	m_get_result_count = [this]() {return m_result.size();};
	m_get_result_pointer = [this](size_t index) {return m_result[index].get();};
	m_get_channel_count = [this]() {return m_total_channels;};
	m_current_weighting = NoWeighting;
	memset(m_db_weighting, 0, sizeof(m_db_weighting));
	return 0;
}

void CThirdOctaveImpl::getDIndex() {
	int dindex_2, index_step;
	if (m_param.type == TOCT_TYPE_LOUDNESS)
		dindex_2 = 22;
	else if (m_param.type == TOCT_TYPE_IEC_SIDE
			|| m_param.type == TOCT_TYPE_ANSI_SIDE)
		dindex_2 = 23;
	else
		dindex_2 = 24;
	m_dindex_2 = dindex_2;
	if (m_param.type != TOCT_TYPE_IEC_ALL) {
		m_oct_channels = 3;
		index_step = 2;
	} else {
		m_oct_channels = 6;
		index_step = 1;
	}
	// physical filter center frequencies which differ from formal standardized values
	double f_fnyq, factor;
	f_fnyq = 1000.0 * pow(2.0, (double) dindex_2 / 6.0) * 2.0 / sample_rate;
	factor = pow(2.0, (double) (-index_step) / 6.0);

	for (int k = 0; k != m_oct_channels; k++) {
		TOCT_FILTERS[k].dindex_2 = dindex_2;
		TOCT_FILTERS[k].f_fnyq = f_fnyq;//1000.0 * pow(2.0, (double)dindex_2/6.0) / fnyq;
		dindex_2 -= index_step;
		f_fnyq *= factor;
	}
}

void CThirdOctaveImpl::initToctFilters() {
	// init Filters
	getDIndex();

	// used for Loudness
	if (m_param.type == TOCT_TYPE_LOUDNESS) {
		copyToctCoeffs(&TOCT_FILTERS[0], LOUD_UPP_FW, LOUD_UPP_FB);
		copyToctCoeffs(&TOCT_FILTERS[1], LOUD_MID_FW, LOUD_MID_FB);
		copyToctCoeffs(&TOCT_FILTERS[2], LOUD_LOW_FW, LOUD_LOW_FB);	//double fa_fs = TOCT_FILTERS[0].f_fnyq * pow( 2.0, -5/3.0);getMonoStopChebyCoeffs( 10, 0.02, fa_fs, CUT_FW, CUT_FB );
		copyToctCoeffs(&TOCT_DOWN, LOUD_CUT_FW, LOUD_CUT_FB);
	} else	// 30 IEC filters
	{
		if (m_param.type == TOCT_TYPE_IEC_MAIN) {
			copyToctCoeffs(&TOCT_FILTERS[0], IEC_MAIN_UPP_FW, IEC_MAIN_UPP_FB);
			copyToctCoeffs(&TOCT_FILTERS[1], IEC_MAIN_MID_FW, IEC_MAIN_MID_FB);
			copyToctCoeffs(&TOCT_FILTERS[2], IEC_MAIN_LOW_FW, IEC_MAIN_LOW_FB);
			frame_delays_pointer = IEC_MAIN_DELAY_MS;
		}	// 30 ANSI filters
		else if (m_param.type == TOCT_TYPE_ANSI_MAIN) {
			copyToctCoeffs(&TOCT_FILTERS[0], ANSI_MAIN_UPP_FW,
					ANSI_MAIN_UPP_FB);
			copyToctCoeffs(&TOCT_FILTERS[1], ANSI_MAIN_MID_FW,
					ANSI_MAIN_MID_FB);
			copyToctCoeffs(&TOCT_FILTERS[2], ANSI_MAIN_LOW_FW,
					ANSI_MAIN_LOW_FB);
			frame_delays_pointer = ANSI_MAIN_DELAY_MS;
		}	// 30 IEC filters but shifted by 1/3octave
		else if (m_param.type == TOCT_TYPE_IEC_SIDE) {
			copyToctCoeffs(&TOCT_FILTERS[0], IEC_SIDE_UPP_FW, IEC_SIDE_UPP_FB);
			copyToctCoeffs(&TOCT_FILTERS[1], IEC_SIDE_MID_FW, IEC_SIDE_MID_FB);
			copyToctCoeffs(&TOCT_FILTERS[2], IEC_SIDE_LOW_FW, IEC_SIDE_LOW_FB);
			frame_delays_pointer = IEC_SIDE_DELAY_MS;
		}	// 60 IEC filter (standard and shifted)
		else if (m_param.type == TOCT_TYPE_IEC_ALL) {
			copyToctCoeffs(&TOCT_FILTERS[0], IEC_MAIN_UPP_FW, IEC_MAIN_UPP_FB);
			copyToctCoeffs(&TOCT_FILTERS[1], IEC_SIDE_UPP_FW, IEC_SIDE_UPP_FB);
			copyToctCoeffs(&TOCT_FILTERS[2], IEC_MAIN_MID_FW, IEC_MAIN_MID_FB);
			copyToctCoeffs(&TOCT_FILTERS[3], IEC_SIDE_MID_FW, IEC_SIDE_MID_FB);
			copyToctCoeffs(&TOCT_FILTERS[4], IEC_MAIN_LOW_FW, IEC_MAIN_LOW_FB);
			copyToctCoeffs(&TOCT_FILTERS[5], IEC_SIDE_LOW_FW, IEC_SIDE_LOW_FB);
			frame_delays_pointer = IEC_MAIN_SIDE_DELAY_MS;
		}

		copyToctCoeffs(&TOCT_DOWN, CUT_FW, CUT_FB);
	}

	// averaging lowpasses for every filter
	for (int c = 0; c != m_oct_channels; c++) {	// average lowpass for each channel (third-octave)
		makeLowPasses(c);
	}
}

void CThirdOctaveImpl::copyToctCoeffs(toct_struct *toct, double fw[][3],
		double fb[][3]) {
	unsigned char k, order;

	toct->gain = 1.0;
	toct->sos = 0;
	for (k = 0; k < MAX_NUM_SOS; k++) {
		// set the coefficient pointers of each 2.order section to the sections in the static arrays
		toct->fw[k] = fw[k];
		toct->fb[k] = fb[k];
		if (fw[k][0] == 0.0)
			break;

		// num.of second order sections (sos)
		toct->sos++;

		// find filter order
		order = 2;
		while (toct->fw[k][order] == 0.0)
			order--;
		toct->fw_orders[k] = order;
		order = 2;
		while (toct->fb[k][order] == 0.0)
			order--;
		toct->fb_orders[k] = order;

		// overall filter gain at center frequency for normalizing
		toct->gain *= abs(
				z2f(toct->fw[k], toct->fb[k], toct->f_fnyq, toct->fw_orders[k],
						toct->fb_orders[k]));
	}

	// normalize gain at center frequency to 1
	for (k = 0; k < 3; k++)
		toct->fw[0][k] /= toct->gain;		//toct->gain = 1.0;
}

/*! Lowpasses for power envelope and anti-alising acc. to the output interval
 */
void CThirdOctaveImpl::makeLowPasses(int oct_channel) {
	sos_struct *pows = TOCT_POWERS[oct_channel];

	// all octaves for one channel (3 or 6 channels per octave)
	for (int oct = 0; oct != m_num_octaves; oct++) {
		// for Loudness
		if (m_param.method == TAU_LOUDNESS)
			setCawLoudLowPass(&pows[oct], oct);
		else if (m_param.method == TAU_VIPER_AUTO) {// 3dB cut-off frequencies linear rel.to center frequency
			// upper cut-off limit due to the output interval
			setViperLowPass(&pows[oct], oct, TOCT_FILTERS[oct_channel].f_fnyq);
		} else if (m_param.method != TAU_IS_ZERO) {	// Equal Time or Equal Confidence averaging as done in the CAW
			setCawToctLowPass(&pows[oct], oct, m_param.time_constant,
					m_param.method, TOCT_FILTERS[oct_channel].f_fnyq);
		}
	}
}

/*! 3dB-Cut-off frequencies linear rel. to the center frequency with an upper
 * limit due to the output interval. Time Constant tau is related to
 * 1/centerFrequency like Equal Confidence (below a certain frequency).
 */
double CThirdOctaveImpl::setViperLowPass(sos_struct *sos, int oct,
		double cf_fnyq) {
	// sample rate at this octave. Oktave 0 is at 48kHz
	double fs = 48000, fmid_hz, fcut_hz, bw_factor = 0.25, fcut_max_hz =
			m_fs_out * bw_factor;
	for (unsigned char k = 0; k < oct; k++)
		fs /= 2.0;

	// bw = (2^(1/3)-1)/(2^(1/6))
	fmid_hz = cf_fnyq * fs / 2.0;
	fcut_hz = 0.23 * bw_factor * fmid_hz;
	if (fcut_hz > fcut_max_hz)
		fcut_hz = fcut_max_hz;

	sos->fb_order = 1;
	sos->fw_order = 1;
	sos->gain = 1.0;
	GetLowPassCoeffs(sos->fw, sos->fb, fs, fcut_hz);

	// sos->gain = cmpabs( z2f( sos->fw, sos->fb, sos->f_fnyq, 1, 1 ));
	// time constant tau
	return 0.5 / M_PI / fcut_hz;
}

/*! envelope computation using the CAW lowpasses
 */
void CThirdOctaveImpl::setCawToctLowPass(sos_struct *sos, int oct, double tau,
		ThirdOctaveAvarageMethod tau_method, double cf_fnyq) {
	sos->fw_order = 1;
	sos->fb_order = 1;
	sos->gain = 1.0;

	// samplerate at this octave
	double fs = 48000;
	for (char k = 0; k < oct; k++)
		fs /= 2.0;

	if (tau_method == TAU_EQUAL_TIME) {
		// equal time constant for all channels
		GetLowPassCoeffs(sos->fw, sos->fb, fs, 0.5 / M_PI / tau); // , sos->fb_order);
	} else	// ( if tau_method == TAU_EQUAL_CONF )
	{
		// define time constant at octave 4 (fs=3000, fterz=1000Hz)
		// the same coefficients are used for all channels
		//getLowPassCoeffs( sos->fw, sos->fb, 3000, 0.5/PI/tau );//, sos->fb_order );
		// 
		//tau /= 16.0; // /= pow(2,4)
		//getLowPassCoeffs( sos->fw, sos->fb, 48000, 0.5/PI/tau );//, sos->fb_order );

		// design every lowpass acc. to the given tau at 1kHz and tau=k/f 
		tau *= (1000.0 / cf_fnyq / fs * 2.0);
		GetLowPassCoeffs(sos->fw, sos->fb, fs, 0.5 / M_PI / tau);//, sos->fb_order );
	}

	// check filter
	if (sos->fb[1] >= 0.0) {
		sos->fb[1] = 0.0;
		sos->fw[0] = 0.5;
		sos->fw[1] = 0.5;
		sos->fb_order = 0;
	}

	/* if ( tau*fs <= 0.5 ) tau = 0.5/fs+0.001;
	 * sos->fb[0] = 1.0;
	 * sos->fb[1] = 1.0/fs/tau-1.0;
	 * sos->gain = 2.0/(1+sos->fb[1]);
	 * sos->fw[0] = 1.0/sos->gain;
	 * sos->fw[1] = 1.0/sos->gain;
	 */
}

/*! CAW Loudness averaging
 */
void CThirdOctaveImpl::setCawLoudLowPass(sos_struct *sos, int oct) {
	int n[] = { 7, 6, 5, 4, 3, 2, 2, 2, 2, 2 };

	// double fw_tmp[] = {1,1,1};
	// fcut = [60 60 60 60 60 60 30 15 7.5 3.8];
	sos->fb[0] = 1;
	sos->fb[1] = 1.0 / pow(2., n[oct]) - 1.0;
	sos->fb[2] = 0.0;

	sos->fb_order = 1;
	sos->fw_order = 2;
	sos->gain = 3.0 / (1.0 + sos->fb[1]);

	// sos->gain = cmpabs( z2f( fw_tmp , sos->fb, 0, sos->fw_order, sos->fb_order ));
	sos->fw[0] = 1.0 / sos->gain;
	sos->fw[1] = 1.0 / sos->gain;
	sos->fw[2] = 1.0 / sos->gain;
}

void CThirdOctaveImpl::setFrameDelays() {
	// absolute delays in ms rounded to output samples
	for (int c = 0; c != m_total_channels; c++)
		frame_delays[c] = static_cast<short>(round(
				frame_delays_pointer[c] / 1000.0 * m_fs_out));
}

int CThirdOctaveImpl::Calculate(const int* buffer, uint32_t samples) {
	for (uint32_t i = 0; i != samples; ++i) {
		getFrame(buffer[i], m_frame);
		++m_current_sample;
		if (m_current_sample == m_sample_interval) {
			//save result
			m_get_frame_levels(m_frame);
			if (m_param.delay_flag) {
				size_t count = m_get_result_count();
				const float* pData = m_get_result_pointer(count - 1);
				UpdateFrameResult(pData);
			}
			m_current_sample = 0;
		}
	}
	if (samples == 0 && m_param.delay_flag)	// for delay frame
			{
		m_get_frame_levels(m_frame);
		const float* result = m_get_result_pointer(m_get_result_count() - 1);
		int end = static_cast<int>(m_get_result_count()) - 1;
		int nChannels = m_get_channel_count();
		int last_delay = 0;
		for (int i = 0; i != nChannels; ++i) {
			if (frame_delays[i] == last_delay)
				continue;
			//int start_index = end - frame_delays[i];
			//for (int j = start_index; j != end; ++j)
			//{
			//	m_get_result_pointer(j)[nChannels - 1 - i] = result[nChannels - 1 - i];
			//}
			int start_index = end - frame_delays[i];
			for (int j = start_index; j != end - last_delay; ++j) {
				float* result_data = m_get_result_pointer(j);
				for (int k = 0; k != nChannels - i; ++k) {
					result_data[k] = result[k];
				}
			}
			last_delay = frame_delays[i];
		}
		if (!m_result.empty())
			m_result.pop_back();
	}
	return 0;
}

/*! compute all 1/3rd or 1/6th octaves for sample s
 * \param s input sample
 * \param frame: array of third (or 1/6th) octave
 * \return 1 if all octaves are computed
 * the sample is modified (low pass filtered) by filterOctave to be
 * used by the next lower octave filter after decimation (one out of two).
 * Computation of lowest octave happens only one out of 2^(noct-1) times.
 */
int CThirdOctaveImpl::getFrame(double s, double *frame) {

	// all octaves, squaring and averaging
	for (int oct = 0; oct != m_num_octaves; oct++) {
		if (oct == 0) {	// always compute first octave
			s = filterOctave(s, oct, frame);
		} else if (octave_flag[oct - 1])	// previous octave was computed
		{// compute only if not computed last time (compute each octave every 2. time)
			if (!octave_flag[oct]) {
				// filter sample with 3 or 6 filters
				s = filterOctave(s, oct, &frame[oct * m_oct_channels]);
				octave_flag[oct] = true; // computed

				//rto test
				for (int ii = 0; ii != m_oct_channels; ++ii)
					TOC_values[oct * m_oct_channels + ii] += frame[oct
							* m_oct_channels + ii];
				//rto test

				if (oct == m_num_octaves - 1)
					return 1;			// all octaves computed for this sample
			} else	// no more octaves for this sample
			{
				octave_flag[oct] = false;
				break;
			}
		}
	}

	return 0;			// not all octaves computed for this sample
}

/*! filter all channels per octave and anti-aliasing lowpass filtering for the next lower octave
 * \return low pass filtered sample suitable for  filtering  the next octave
 * (one sample will be used for two returned)
 */
double CThirdOctaveImpl::filterOctave(double s, int oct, double *frame) {
	// Third octaves (3 or 6 channels)
	for (int k = 0; k != m_oct_channels; k++) {
		*frame++ = filterCascade(s, &TOCT_FILTERS[k], &TOCT_POWERS[k][oct],
				oct);
	}

	// Downsampling
	if (oct < m_num_octaves - 1) {
		for (int k = 0; k < TOCT_DOWN.sos; k++)
			s = filter(TOCT_DOWN.fw[k], TOCT_DOWN.fb[k], s,
					TOCT_DOWN.states[oct][k], 2, 2);
	}	// gain at center frequency must be already normalized to 1!

	return s;
}

/*! third-octave filtering + envelope computation and calibration
 */
double CThirdOctaveImpl::filterCascade(double s, toct_struct *toct,
		sos_struct *pow, int oct) {
	// filter Second Order Sections of the Third-Octave filter
	for (int k = 0; k < toct->sos; k++) {
		s = filter(toct->fw[k], toct->fb[k], s, toct->states[oct][k],
				toct->fw_orders[k], toct->fb_orders[k]);
	}	// gain at center frequency must be already normalized to 1!

	// squaring
	s *= s;

	// averaging
	if (m_param.method != TAU_IS_ZERO)
		s = filter(pow->fw, pow->fb, s, pow->states, pow->fw_order,
				pow->fb_order);

	// Calibration
	return (s / m_Pref);
}

// computes level from power
double CThirdOctaveImpl::getLevel(double p_pref) {
	if (p_pref > m_PMax)
		return m_LMax;
	else if (p_pref < m_PMin)
		return m_LMin;
	return 10.0 * log10(p_pref);
	/*if (p_pref > m_PMax)
	 return (32767.0);
	 else if (p_pref < m_PMin)
	 return (-32768.0);
	 else
	 {
	 p_pref = 10.0*log10(p_pref);
	 p_pref /= k_l02_factor_l;
	 if (p_pref < 0.0)
	 return (p_pref - 0.5);
	 else
	 return (p_pref + 0.5);
	 }*/
}

void CThirdOctaveImpl::getFrameLevels(const double *frame) {
	double tmp;
	int d = m_total_channels - 1; // lbl AI
	m_result.emplace_back(new float[m_total_channels]);
	for (int c = 0; c < m_total_channels; c++) {
		tmp = getLevel(frame[d--]);

		if (tmp > m_tocts_max)
			m_tocts_max = tmp;

		if (tmp < m_tocts_min)
			m_tocts_min = tmp;

		// convert into short
		m_result.back()[c] = static_cast<float>(tmp);
	}
}

void CThirdOctaveImpl::UpdateFrameResult(const float* frame) {
	int channels = m_get_channel_count();
	int index = static_cast<int>(m_get_result_count() - 1);
	for (int j = 0, delay_index = channels - 1; j != channels;
			++j, --delay_index) {
		if (frame_delays[delay_index] == 0)
			break;
		int upd_index = index - frame_delays[delay_index];
		if (upd_index >= 0) {
			m_get_result_pointer(upd_index)[j] = frame[j];
		}
	}
}

int CThirdOctaveImpl::GetMeanMaxResult(bool bMean, FreqWeighting weighting,
		double* data_time, double* data_freq) {
	unique_ptr<double[]> mean_temp(new double[m_total_channels]);
	unique_ptr<double[]> mean_total(new double[m_total_channels]());
	SetWeighting(weighting);
	int index = 0;
	double p;
	for (auto it = m_result.begin(); it != m_result.end(); ++it, ++index) {
		for (int i = 0; i != m_total_channels; ++i) {
			mean_temp[i] = (*it)[i] * k_l02_factor_l + m_db_weighting[i];
			mean_temp[i] = pow(10.0, mean_temp[i] / 10.0);
		}
		if (bMean) {
			p = accumulate(mean_temp.get(), mean_temp.get() + m_total_channels,
					0.0);
			for (int i = 0; i != m_total_channels; ++i)
				mean_total[i] += mean_temp[i];
		} else {
			p = *(max_element(mean_temp.get(),
					mean_temp.get() + m_total_channels, [](double a, double b)
					{
						if (abs(a) < abs(b))
						return true;
						return false;
					}));
			for (int i = 0; i != m_total_channels; ++i) {
				if (abs(mean_temp[i] > abs(mean_total[i])))
					mean_total[i] = mean_temp[i];
			}
		}
		if (data_time) {
			p = 10.0 * log10(p);
			data_time[index] = p;
		}
	}
	if (bMean) {
		for (int i = 0; i != m_total_channels; ++i)
			mean_total[i] /= m_result.size();
	}
	if (data_freq) {
		for (int i = 0; i != m_total_channels; ++i)
			data_freq[i] = 10.0 * log10(mean_total[i]);
	}
	return 0;
}

void CThirdOctaveImpl::SetWeighting(FreqWeighting weighting) {
	if (weighting == m_current_weighting)
		return;
	if (weighting == NoWeighting) {
		memset(m_db_weighting, 0, sizeof(m_db_weighting));
	} else {
		double max_band_index = 12, delta_band = 1;
		if (m_param.type == TOCT_TYPE_IEC_SIDE
				|| m_param.type == TOCT_TYPE_ANSI_SIDE)
			max_band_index = 11.5;
		else if (m_param.type == TOCT_TYPE_ANSI_ALL
				|| m_param.type == TOCT_TYPE_IEC_ALL)
			delta_band = 0.5;
		double m, f = 1000 * pow(2.0, max_band_index / 3.0), factor = pow(2.0,
				-delta_band / 3.0);
		for (int index = m_total_channels - 1; index != 0; --index) {
			m = GetAbcMagnitude(f, weighting);
			m_db_weighting[index] = 20 * log10(m);
			f *= factor;
		}
	}
	m_current_weighting = weighting;
}

//HTHIRDOCTAVE THIRDOCTAVE_API InitThirdOctave(ThirdOctaveParam param)
//{
//	CThirdOctaveImpl* pOctave = new CThirdOctaveImpl(param);
//	if (pOctave->Init() != 0)
//	{
//		delete pOctave;
//		return NULL;
//	}
//	return pOctave;
//}
//
//void THIRDOCTAVE_API UnInitThirdOctave(HTHIRDOCTAVE handle)
//{
//	CThirdOctaveImpl* pOctave = static_cast<CThirdOctaveImpl*>(handle);
//	delete pOctave;
//}
//
//int THIRDOCTAVE_API CalculateThirdOctave(HTHIRDOCTAVE handle, const int32_t* buffer, uint32_t samples)
//{
//	CThirdOctaveImpl* pOctave = static_cast<CThirdOctaveImpl*>(handle);
//	return pOctave->Calculate(buffer, samples);
//}
//
//bool THIRDOCTAVE_API InitForAICalculate(HTHIRDOCTAVE handle, std::function<void(const double*)> GetFrameLevels)
//{
//	CThirdOctaveImpl* pOctave = static_cast<CThirdOctaveImpl*>(handle);
//	return pOctave->InitAiCalculate(GetFrameLevels);
//}
//
//
//int THIRDOCTAVE_API GetCalculateChannelCount(HTHIRDOCTAVE handle)
//{
//	CThirdOctaveImpl* pOctave = static_cast<CThirdOctaveImpl*>(handle);
//	return pOctave->GetCalculateChannelCount();
//}
//
//
//int THIRDOCTAVE_API GetThirdOctaveCalcResultCount(HTHIRDOCTAVE handle, uint32_t* count, uint32_t* frame_size)
//{
//	CThirdOctaveImpl* pOctave = static_cast<CThirdOctaveImpl*>(handle);
//	return pOctave->GetCalcResultCount(count, frame_size);
//}
//
//int THIRDOCTAVE_API GetThirdOctaveCalcResult(HTHIRDOCTAVE handle, uint32_t index, float* frame)
//{
//	CThirdOctaveImpl* pOctave = static_cast<CThirdOctaveImpl*>(handle);
//	return pOctave->GetCalcResult(index, frame);
//}
//
//int THIRDOCTAVE_API GetThirdOctaveMeanMaxResultCount(HTHIRDOCTAVE handle, uint32_t* count_time, uint32_t* count_freq)
//{
//	CThirdOctaveImpl* pOctave = static_cast<CThirdOctaveImpl*>(handle);
//	return pOctave->GetMeanMaxResultCount(count_time, count_freq);
//}
//
//int THIRDOCTAVE_API GetThirdOctaveMeanMaxResult(HTHIRDOCTAVE handle, bool bMean, FreqWeighting weighting, double* data_time, double* data_frq)
//{
//	CThirdOctaveImpl* pOctave = static_cast<CThirdOctaveImpl*>(handle);
//	return pOctave->GetMeanMaxResult(bMean, weighting, data_time, data_frq);
//}
//
//
//const double THIRDOCTAVE_API *GetToctFrame(HTHIRDOCTAVE handle)
//{
//	CThirdOctaveImpl* pOctave = static_cast<CThirdOctaveImpl*>(handle);
//	return pOctave->GetToctFrame();
//}

CThirdOctave::CThirdOctave(ThirdOctaveParam param) :
		m_pImpl(new CThirdOctaveImpl(param)) {
	static_cast<CThirdOctaveImpl*>(m_pImpl)->Init();
}

CThirdOctave::~CThirdOctave() {
	delete static_cast<CThirdOctaveImpl*>(m_pImpl);
}

int CThirdOctave::Calculate(const int* data, uint32_t samples) {
	return static_cast<CThirdOctaveImpl*>(m_pImpl)->Calculate(data, samples);
}

CToctEx* CThirdOctave::GetEx() {
	CToctEx* pEx = new CToctEx(static_cast<CThirdOctaveImpl*>(m_pImpl));
	return pEx;
}

void CThirdOctave::ReleaseEx(CToctEx* pEx) {
	delete pEx;
}

uint32_t CThirdOctave::GetResultInfo(uint32_t* channels) {
	return static_cast<CThirdOctaveImpl*>(m_pImpl)->GetResultInfo(channels);
}

const float* CThirdOctave::GetResult(uint32_t index) {
	return static_cast<CThirdOctaveImpl*>(m_pImpl)->GetResult(index);
}

void CThirdOctave::SetWeighting(FreqWeighting weighting) {
	return static_cast<CThirdOctaveImpl*>(m_pImpl)->SetWeighting(weighting);
}

const float* CThirdOctave::GetMeanResult(uint32_t index) {
	return static_cast<CThirdOctaveImpl*>(m_pImpl)->GetMeanResult(index);
}

void CThirdOctave::GetFinalMean(const float* data, float* output,
		uint32_t count) {
	return static_cast<CThirdOctaveImpl*>(m_pImpl)->GetFinalMean(data, output,
			count);
}
ThirdOctaveParam CThirdOctave::GetM_param() {
return static_cast<CThirdOctaveImpl*>(m_pImpl)->GetM_param();
}
void CThirdOctave::SetM_param(ThirdOctaveParam param){
return static_cast<CThirdOctaveImpl*>(m_pImpl)->SetM_param(param);
}
double CToctEx::GetLevel(double power) {
return static_cast<CThirdOctaveImpl*>(m_pImpl)->getLevel(power);
}

int CToctEx::GetCalculateChannelCount() {
return static_cast<CThirdOctaveImpl*>(m_pImpl)->GetCalculateChannelCount();
}

CToctEx::CToctEx(CThirdOctaveImpl* pImpl) :
	m_pImpl(pImpl) {

}

void CToctEx::InitEx(std::function<void(const double*)> GetFrameLevels) {
m_pImpl->InitAiCalculate(GetFrameLevels);
}

void CToctEx::InitResultFunc(std::function<size_t()> GetResultCount,
	std::function<float*(size_t index)> GetResultPointer,
	std::function<int()> GetChannelCount) {
m_pImpl->InitResultFunc(GetResultCount, GetResultPointer, GetChannelCount);
}

const double* CToctEx::GetCurrentFrame() {
return m_pImpl->GetToctFrame();
}
#ifdef __cplusplus
}
#endif
