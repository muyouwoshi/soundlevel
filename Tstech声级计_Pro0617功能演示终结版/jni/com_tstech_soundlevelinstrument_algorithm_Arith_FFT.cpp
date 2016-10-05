#include "com_tstech_soundlevelinstrument_algorithm_Arith_FFT.h"
#include "fft.h"
#include "rpm.h"
#include "AddWeighting.h"
#include <functional>
#include <numeric>
#include <vector>
#include <algorithm>
#include <assert.h>
#include "palette.h";
bool ifChangeParam;
jint native;
using namespace std;
JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_init(
		JNIEnv * env, jobject obj) {
	FFTParam param;
	if (!ifChangeParam) {
		InitFFtParam(param);
		CFFTCalc *calc = new CFFTCalc(param);
		return (int) calc;
	} else {
		return (int) native;
	}
}

JNIEXPORT void JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_finalizer(JNIEnv * env,
		jobject obj, jint classz) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	delete calc;
}

JNIEXPORT void JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1Int_1Calculate(
		JNIEnv * env, jobject obj, jint classz, jintArray buf, jint samples) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	if (buf != NULL && samples != 0) {
		const int* data = env->GetIntArrayElements(buf, false);
//		calc->ResultClear();
		calc->Calculate(data, samples);
		env->ReleaseIntArrayElements(buf, (jint*) data, 0);
	} else {
		const int* data=NULL;
		calc->Calculate(data, 0);
	}
}

JNIEXPORT void JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1Float_1Calculate(
		JNIEnv * env, jobject obj, jint classz, jfloatArray buf, jint samples) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	if (buf != NULL && samples != 0) {
		const float* data = env->GetFloatArrayElements(buf, false);
//		calc->ResultClear();
		calc->Calculate(data, samples);
		env->ReleaseFloatArrayElements(buf, (jfloat*) data, 0);
	}
}
JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1GetResultInfo(
		JNIEnv * env, jobject obj, jint classz, jint channels) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	uint32_t* count = (uint32_t*) &channels;
	return calc->GetResultInfo(count);
}

JNIEXPORT jfloatArray JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1GetResult(
		JNIEnv * env, jobject obj, jint classz, jint index) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	uint32_t size = 0;
	calc->GetResultInfo(&size);
	if (size > 0) {
		jfloatArray result = env->NewFloatArray(size);
		float* data = env->GetFloatArrayElements(result, false);
		if (calc->GetResult((uint32_t) index, data)) {
			env->SetFloatArrayRegion(result, 0, size, data);
			env->ReleaseFloatArrayElements(result, (jfloat*) data, 0);
			return result;
		} else {
			env->ReleaseFloatArrayElements(result, (jfloat*) data, 0);
		}
	}
	return NULL;
}

JNIEXPORT void JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1GetMinMaxValue(
		JNIEnv * env, jobject obj, jint classz, jfloat min_value,
		jfloat max_value) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	calc->GetMinMaxValue(min_value, max_value);
}
JNIEXPORT jintArray JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1GetPalette(
		JNIEnv * env, jobject obj, jint nID) {
	uint32_t* result = GetPalette(nID);
	jintArray data = env->NewIntArray(256);
	env->SetIntArrayRegion(data, 0, 256, (jint*) result);
	return data;
}
JNIEXPORT jfloatArray JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1GetMeanResult(
		JNIEnv * env, jobject obj, jint classz) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	uint32_t m_fft_channels = 0;

	calc->GetResultInfo(&m_fft_channels);
	float* dataFloat = new float[m_fft_channels];
	if (calc->GetMeanResult(dataFloat)) {
		jfloatArray data = env->NewFloatArray(m_fft_channels);
		env->SetFloatArrayRegion(data, 0, m_fft_channels,
				(const jfloat*) dataFloat);
		delete[] dataFloat;
		return data;
	} else {
		delete[] dataFloat;
		return NULL;
	}
}
JNIEXPORT void JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1ResetMeanResult(
		JNIEnv * env, jobject obj, jint classz) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	calc->ResetMeanResult();
}
JNIEXPORT void JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1ResetResult
(JNIEnv * env, jobject obj, jint classz) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	calc->ResultClear();
}
JNIEXPORT void JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1ResetSignal
(JNIEnv * env, jobject obj, jint classz) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	calc->Reset();
}
JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1SetWindowType(
		JNIEnv * env, jobject obj, jint classz, jint windowType) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	FFTParam param = calc->GetFFTParam();
	param.type = (WindowType) windowType;

	CFFTCalc *fftcalc = new CFFTCalc(param);
	fftcalc->SetFFTParam(param);
	ifChangeParam = true;
	native = (jint) fftcalc;
	return native;
}

JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1SetWinShift(
		JNIEnv * env, jobject obj, jint classz, jint winShift) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	FFTParam param = calc->GetFFTParam();
	param.winshift = winShift;

	CFFTCalc *fftcalc = new CFFTCalc(param);
	fftcalc->SetFFTParam(param);
	ifChangeParam = true;
	native = (jint) fftcalc;
	return native;
}

JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1SetWinLen(
		JNIEnv * env, jobject obj, jint classz, jint winLen) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	FFTParam param = calc->GetFFTParam();
	param.winlen = winLen;

	CFFTCalc *fftcalc = new CFFTCalc(param);
	fftcalc->SetFFTParam(param);
	ifChangeParam = true;
	native = (jint) fftcalc;
	return native;
}

JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1GetWinLen(
		JNIEnv * env, jobject obj, jint classz) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	FFTParam param = calc->GetFFTParam();
	jint winlenn = (jint) param.winlen;
	return winlenn;
}

JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1GetWinShift(
		JNIEnv * env, jobject obj, jint classz) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	FFTParam param = calc->GetFFTParam();
	jint winshiftt = (jint) param.winshift;
	return winshiftt;
}
JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1GetWindowType(
		JNIEnv *, jobject, jint classz) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	FFTParam param = calc->GetFFTParam();
	jint wintype = (jint) param.type;
	return wintype;
}
JNIEXPORT jfloatArray JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_setWeight(
		JNIEnv * env, jobject obj, jint classz, jdouble samplerate,
		jfloatArray data, jint datacount, jint weighting) {
	if (data != NULL) {
		const float* result = env->GetFloatArrayElements(data, false);
		GetFFTWeightingResult(samplerate, result, datacount, weighting);
		env->SetFloatArrayRegion(data, 0, datacount, result);
		env->ReleaseFloatArrayElements(data, (jfloat*) result, 0);
		return data;
	} else {
		return NULL;
	}
}

JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1setRange(
		JNIEnv * env, jobject obj, jint classz, jdouble range) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	FFTParam param = calc->GetFFTParam();
	param.range = range;

	CFFTCalc *fftcalc = new CFFTCalc(param);
	fftcalc->SetFFTParam(param);
	ifChangeParam = true;
	native = (jint) fftcalc;
	return native;
}

JNIEXPORT jdouble JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1getRange(
		JNIEnv * env, jobject obj, jint classz) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	FFTParam param = calc->GetFFTParam();
	jdouble range = (jdouble) param.range;
	return range;
}

JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1FFT_native_1setSimpleRate(
		JNIEnv * env, jobject obj, jint classz, jdouble samplerate) {
	CFFTCalc *calc = (CFFTCalc*) classz;
	FFTParam param = calc->GetFFTParam();
	param.samplerate = samplerate;

	CFFTCalc *fftcalc = new CFFTCalc(param);
	fftcalc->SetFFTParam(param);
	ifChangeParam = true;
	native = (jint) fftcalc;
	return native;
}
