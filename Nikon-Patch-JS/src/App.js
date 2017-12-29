import React, { Component } from 'react';
import './App.css';
import { saveAs } from 'file-saver';

/*global _detectFirmware _patch_firmare _getInFilePtr _getOutFilePtr _getJsonPtr _getMaxFileSize _getSelectPtr Module*/


class FirmwareControl extends Component {
    constructor(props){
        super(props);
        this.handleSelectClick = this.handleSelectClick.bind(this);
        this.handleSaveClick = this.handleSaveClick.bind(this);
        this.handlePatchClick = this.handlePatchClick.bind(this);
        var patchSet = new Map()
        this.state = {hasPatchesSelect: false, patchSet:patchSet, patches: []};
    }

    handleSelectClick() {
        var inputfile = document.getElementById('inputfile');
        if (inputfile.files.length === 0)
                return;
        var file = inputfile.files[0];
        var maxFileSize = _getMaxFileSize();
        if (file.size > maxFileSize)
            return;

        var patches = {"model":"Unknown", "version":"Unknown2", "patches":[]};
        var fr = new FileReader();
        fr.onload = function () {
            var data = new Uint8Array(fr.result);
            var data_mem = _getInFilePtr();
            Module.HEAPU8.set(data, data_mem);

            var outcount = _detectFirmware(data.length);
            if(outcount>0){
                var out_mem = _getJsonPtr();
                let s = "";
                for (let i = 0; i < outcount; ++i){
                    s += String.fromCharCode(Module.getValue(out_mem+i));
                }
                patches = JSON.parse(s);
            }
            var patchSet = new Map()
            var pp = patches["patches"];

            pp.forEach((patch) => patchSet.set(patch.id,false) );
            this.setState({patchSet:patchSet, patches: pp, filename: file.name})
        }.bind(this);
        fr.readAsArrayBuffer(file);
    }
    
    handleSaveClick(){
        var selected = [];
        for (var [key, value] of this.state.patchSet.entries()) {
            if( value){
                selected.push(parseInt(key,10));
            }
        }
        var select_ptr = _getSelectPtr();
        Module.HEAPU32.set(selected, select_ptr/4);

        var ret = _patch_firmare(selected.length);

        if(ret>0){
            var outptr = _getOutFilePtr();
            var data = new Uint8Array(Module.HEAPU8.buffer, outptr, ret);
            var blob = new Blob([data], {type: 'binary/octet-stream'});
            saveAs(blob, "patched_"+this.state.filename, true);
        }
    }

    handlePatchClick(id, set){
        var s = this.state.patchSet
        s.set(id,set)
        if(set){
            var patch = this.state.patches.find((p)=>p.id===id);
            if(patch){
                patch.blocks.forEach((b)=>s.set(b,false));
            }
        }
        var a = false
        s.forEach((v) => a|=v)
        this.setState({patchSet:s, hasPatchesSelect:a});
    }

    render(){
        const content = this.state.patches.map((patch) =>
            <PatchRow key={patch.id.toString()} 
                id={patch.id}
                name={patch.name} 
                set={this.state.patchSet.get(patch.id)}
                onTrySet={this.handlePatchClick} />
        );
        return (<div> 
            <input type="file" id="inputfile" name="select file" accept=".bin" onChange={this.handleSelectClick} />
            <button onClick={this.handleSaveClick} disabled={!this.state.hasPatchesSelect}>Save Patched Firmware File</button>
            <hr/>
            <table><tbody>
                {content}
            </tbody></table>
            <hr/>
        </div>
        );
    }
}

class PatchRow extends Component {
    constructor(props) {
        super(props);
        this.handleClick = this.handleClick.bind(this);
    }
    handleClick(e) {
        this.props.onTrySet(this.props.id, !this.props.set);
    }
    render() {
        return (
            <tr className="PatchRow">
                <td><button onClick={this.handleClick}>{this.props.set ? '*': '_'}</button></td>
                <td>{this.props.name}</td>
            </tr>
        );
    }
}

class App extends Component {
  render() {
    return (
      <div className="App">
        <FirmwareControl/>
      </div> 
    );
  }
}

export default App;
