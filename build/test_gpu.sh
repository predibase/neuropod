#!/bin/bash
set -ex

# Use the virtualenv
source .neuropod_venv/bin/activate

# Enable code coverage
export LLVM_PROFILE_FILE="/tmp/neuropod_coverage/code-%p-%9m.profraw"
export COVERAGE_PROCESS_START="`pwd`/source/python/.coveragerc"
echo "import coverage; coverage.process_startup()" > \
    `python -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())"`/coverage.pth

# Override the Neuropod backend base directory
export NEUROPOD_BASE_DIR=`pwd`/.neuropod_test_base

# Enable python isolation
# TODO(vip): Remove this once isolation is default behavior
export NEUROPOD_ENABLE_PYTHON_ISOLATION=true

pushd source

# Make sure we have GPUs
num_gpus=`nvidia-smi -L | wc -l`
if [ "$num_gpus" == "0" ]; then
    echo "Attemped to run tests on GPU, but no GPUs found!"
    exit 1
else
    echo "Found GPUs!"
    # List the GPUs
    nvidia-smi -L
fi

# Add the python library to the pythonpath
export PYTHONPATH=$PYTHONPATH:`pwd`/python

if [[ $(uname -s) == 'Linux' ]]; then
    # On linux we don't want to use GCC5 to build the custom ops
    export TF_CXX=g++-4.8
else
    # For building custom ops
    export MACOSX_DEPLOYMENT_TARGET=10.15
fi

# Run python tests
pushd python
NEUROPOD_LOG_LEVEL=TRACE python -m unittest discover --verbose neuropod

# Run GPU only python tests
NEUROPOD_LOG_LEVEL=TRACE python -m unittest discover --verbose neuropod -p gpu_test*.py
popd

# Run native tests
export PATH=$PATH:`pwd`/bazel-bin/neuropod/multiprocess/

if [ -z ${NEUROPOD_TEST_FRAMEWORKS+x} ]; then
    # Run all tests if NEUROPOD_TEST_FRAMEWORKS is unset
    TEST_TARGETS=$(bazel query "kind(_test, //...)")
else
    # Get all test targets that match our framework filters
    # All tests - tests that require a framework + tests that require any of the available frameworks
    # TODO(vip): this doesn't currently handle tests that require multiple frameworks, but we don't have any of those at the moment
    TEST_TARGETS=$(bazel query "kind(_test, //...) - attr(tags, '\\brequires_framework_', //...) + attr(tags, '\\brequires_framework_(${NEUROPOD_TEST_FRAMEWORKS//,/|})\\b', //...)")
fi

# GPU tests with trace logging
bazel test "$@" --sandbox_writable_path="$HOME/.neuropod/pythonpackages/" --test_lang_filters="-java" --test_tag_filters="-no_trace_logging" --test_env="NEUROPOD_LOG_LEVEL=TRACE" $TEST_TARGETS ||
    # Allow an exit code of 4 (no tests run)
    [ "$?" = 4 ]

# GPU tests without trace logging
bazel test "$@" --sandbox_writable_path="$HOME/.neuropod/pythonpackages/" --test_lang_filters="-java" --test_tag_filters="no_trace_logging" $TEST_TARGETS ||
    # Allow an exit code of 4 (no tests run)
    [ "$?" = 4 ]

# Java GPU tests
bazel test "$@" --sandbox_writable_path="$HOME/.neuropod/pythonpackages/" --combined_report=lcov --test_lang_filters="java" $TEST_TARGETS ||
    # Allow an exit code of 4 (no tests run)
    [ "$?" = 4 ]

popd

# Maybe upload a release
python build/upload_release.py
