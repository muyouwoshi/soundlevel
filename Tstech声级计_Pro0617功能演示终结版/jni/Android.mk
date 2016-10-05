LOCAL_PATH :=	$(call my-dir) 

include   $(CLEAR_VARS)

LOCAL_CPPFLAGS :=--std=c++11     
LOCAL_MODULE    := FFT
LOCAL_SRC_FILES := com_tstech_soundlevelinstrument_algorithm_Arith_FFT.cpp fft.cpp fft_tools.cpp palette.cpp math_ex.cpp AddWeighting.cpp Level.cpp Tools.cpp
LOCAL_LDLIBS := -landroid                        
include  $(BUILD_SHARED_LIBRARY)

include  $(CLEAR_VARS)
LOCAL_CPPFLAGS :=--std=c++11
LOCAL_MODULE    := SPL
LOCAL_SRC_FILES := com_tstech_soundlevelinstrument_algorithm_Arith_SPL.cpp Level.cpp Tools.cpp math_ex.cpp
LOCAL_LDLIBS := -landroid                        
include  $(BUILD_SHARED_LIBRARY)

