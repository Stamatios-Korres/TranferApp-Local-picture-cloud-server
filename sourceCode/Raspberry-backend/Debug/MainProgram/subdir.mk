################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../MainProgram/ClientServer.cpp \
../MainProgram/MainThread.cpp 

OBJS += \
./MainProgram/ClientServer.o \
./MainProgram/MainThread.o 

CPP_DEPS += \
./MainProgram/ClientServer.d \
./MainProgram/MainThread.d 


# Each subdirectory must supply rules for building sources it contributes
MainProgram/%.o: ../MainProgram/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cross G++ Compiler'
	g++ -D__GXX_EXPERIMENTAL_CXX0X__ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


