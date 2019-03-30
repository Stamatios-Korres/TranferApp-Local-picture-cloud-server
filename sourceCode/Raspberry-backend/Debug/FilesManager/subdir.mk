################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../FilesManager/FilesManager.cpp 

OBJS += \
./FilesManager/FilesManager.o 

CPP_DEPS += \
./FilesManager/FilesManager.d 


# Each subdirectory must supply rules for building sources it contributes
FilesManager/%.o: ../FilesManager/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cross G++ Compiler'
	g++ -D__GXX_EXPERIMENTAL_CXX0X__ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


