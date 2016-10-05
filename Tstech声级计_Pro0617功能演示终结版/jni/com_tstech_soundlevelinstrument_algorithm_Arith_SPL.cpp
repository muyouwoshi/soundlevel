#include "com_tstech_soundlevelinstrument_algorithm_Arith_SPL.h"
#include "Level.h"
#include <MeanMax.h>
#include "Common.h"
using namespace std;
bool ifChangeParam;
jint native;
JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_init(
		JNIEnv * env, jobject obj) {
	LevelParam param;
	if (!ifChangeParam) {
		InitLevelParam(param);
		CLevel *leverAve = new CLevel(param);
		return (int) leverAve;
	} else {
		return (int) native;
	}
}
JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_native_1calculate(
		JNIEnv * env, jobject obj, jint classz, jintArray buf, jint samples) {
	CLevel *leverAve = (CLevel*) classz;
	uint32_t samp = (uint32_t) samples;
	if (samp != 0) {
		const int* data = env->GetIntArrayElements(buf, false);
		leverAve->ResetResult();
		int calResult = leverAve->Calculate(data, samp);
		env->ReleaseIntArrayElements(buf, (jint*) data, 0);
		return calResult;
	} else {
		return 0;
	}
}
JNIEXPORT jdoubleArray JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_native_1getResult(
		JNIEnv * env, jobject obj, jint classz) {
	CLevel *leverAve = (CLevel*) classz;
	uint32_t channelSize;
	const double* meanResult = leverAve->GetResult(&channelSize);
	if (channelSize > 0) {
		jdoubleArray result = env->NewDoubleArray(channelSize);
		env->SetDoubleArrayRegion(result, 0, channelSize,
				(jdouble*) meanResult);
		return result;
	} else {
		return NULL;
	}
}
JNIEXPORT jfloatArray JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_native_1getInfo(
		JNIEnv * env, jobject obj, jint classz) {
	CLevel *leverAve = (CLevel*) classz;
	LevelStatistic levStatic = leverAve->GetStatistic();
	float min = levStatic.min;
	float max = levStatic.max;
	float mean = levStatic.mean;
	float info[3];
	info[0] = min;
	info[1] = max;
	info[2] = mean;
	jfloatArray result = env->NewFloatArray(3);
	env->SetFloatArrayRegion(result, 0, 3, (jfloat*) info);
	return result;
}

JNIEXPORT void JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_finalizer(JNIEnv * env,
		jobject obj, jint classz) {
	CLevel *leverAve = (CLevel*) classz;
	delete leverAve;
}

JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_setWeight(
		JNIEnv * env, jobject obj, jint classz, jint weight) {
	CLevel *leverAve = (CLevel*) classz;
	LevelParam param = leverAve->GetM_param();
	switch (weight) {
	case 0:
		param.weighting = (FreqWeighting) NoWeighting;
		break;
	case 1:
		param.weighting = (FreqWeighting) AWeighting;
		break;
	case 2:
		param.weighting = (FreqWeighting) CWeighting;
	}
	CLevel *leverAvee = new CLevel(param);
	leverAvee->SetM_param(param);
	ifChangeParam = true;
	native = (jint) leverAvee;
	return native;
}

JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_setAva(
		JNIEnv * env, jobject obj, jint classz, jint avg) {
	CLevel *leverAve = (CLevel*) classz;
	LevelParam param = leverAve->GetM_param();
	switch (avg) {
	case 0:
		param.avarage = (TimeAvaraging) FastAverage;
		break;
	case 1:
		param.avarage = (TimeAvaraging) SlowAverage;
		break;
	}
	CLevel *leverAvee = new CLevel(param);
	leverAvee->SetM_param(param);
	ifChangeParam = true;
	native = (jint) leverAvee;
	return native;
}
JNIEXPORT void JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_native_1resetResult
(JNIEnv * env, jobject obj, jint classz) {
	CLevel *leverAve = (CLevel*) classz;
	leverAve->ResetResult();
}

JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_native_1setRange(
		JNIEnv * env, jobject obj, jint classz, jdouble range) {
	CLevel *leverAve = (CLevel*) classz;
	LevelParam param = leverAve->GetM_param();
	param.range = range;

	CLevel *leverAvee = new CLevel(param);
	leverAvee->SetM_param(param);
	ifChangeParam = true;
	native = (jint) leverAvee;
	return native;

}

JNIEXPORT jdouble JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_native_1getRange(
		JNIEnv * env, jobject obj, jint classz) {
	CLevel *leverAve = (CLevel*) classz;
	LevelParam param = leverAve->GetM_param();
	jdouble range = (jdouble) param.range;
	return range;
}
JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_native_1setSamplerate(
		JNIEnv * env, jobject obj, jint classz, jdouble samplerate) {
	CLevel *leverAve = (CLevel*) classz;
	LevelParam param = leverAve->GetM_param();
	param.samplerate = samplerate;

	CLevel *leverAvee = new CLevel(param);
	leverAvee->SetM_param(param);
	ifChangeParam = true;
	native = (jint) leverAvee;
	return native;

}

JNIEXPORT jint JNICALL Java_com_tstech_soundlevelinstrument_algorithm_Arith_1SPL_native_1setSimpleRate(
		JNIEnv * env, jobject obj, jint classz, jdouble samplerate) {
	CLevel *leverAve = (CLevel*) classz;
	LevelParam param = leverAve->GetM_param();
	param.samplerate = samplerate;


	CLevel *leverAvee = new CLevel(param);
	leverAvee->SetM_param(param);
	ifChangeParam = true;
	native = (jint) leverAvee;
	return native;
}
