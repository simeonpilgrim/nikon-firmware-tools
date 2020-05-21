namespace Nikon_Patch
{
    partial class MainForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.DecodeFile_btn = new System.Windows.Forms.Button();
            this.EncodeFile_btn = new System.Windows.Forms.Button();
            this.PatchFile_btn = new System.Windows.Forms.Button();
            this.ExportPatches_btn = new System.Windows.Forms.Button();
            this.tFirmwareName = new System.Windows.Forms.Label();
            this.tFirmwareVersion = new System.Windows.Forms.Label();
            this.tFeature = new System.Windows.Forms.Label();
            this.lstFeatures = new System.Windows.Forms.ListBox();
            this.bSaveFirmware = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // DecodeFile_btn
            // 
            this.DecodeFile_btn.Location = new System.Drawing.Point(12, 12);
            this.DecodeFile_btn.Name = "DecodeFile_btn";
            this.DecodeFile_btn.Size = new System.Drawing.Size(75, 23);
            this.DecodeFile_btn.TabIndex = 0;
            this.DecodeFile_btn.Text = "Decode File";
            this.DecodeFile_btn.UseVisualStyleBackColor = true;
            this.DecodeFile_btn.Click += new System.EventHandler(this.DecodeButton_Click);
            // 
            // EncodeFile_btn
            // 
            this.EncodeFile_btn.Enabled = false;
            this.EncodeFile_btn.Location = new System.Drawing.Point(93, 12);
            this.EncodeFile_btn.Name = "EncodeFile_btn";
            this.EncodeFile_btn.Size = new System.Drawing.Size(75, 23);
            this.EncodeFile_btn.TabIndex = 1;
            this.EncodeFile_btn.Text = "Encode File";
            this.EncodeFile_btn.UseVisualStyleBackColor = true;
            // 
            // PatchFile_btn
            // 
            this.PatchFile_btn.Location = new System.Drawing.Point(174, 12);
            this.PatchFile_btn.Name = "PatchFile_btn";
            this.PatchFile_btn.Size = new System.Drawing.Size(75, 23);
            this.PatchFile_btn.TabIndex = 2;
            this.PatchFile_btn.Text = "Patch File";
            this.PatchFile_btn.UseVisualStyleBackColor = true;
            this.PatchFile_btn.Click += new System.EventHandler(this.PatchFileButton_Click);
            // 
            // ExportPatches_btn
            // 
            this.ExportPatches_btn.Location = new System.Drawing.Point(255, 12);
            this.ExportPatches_btn.Name = "ExportPatches_btn";
            this.ExportPatches_btn.Size = new System.Drawing.Size(118, 23);
            this.ExportPatches_btn.TabIndex = 3;
            this.ExportPatches_btn.Text = "Export Patches to C";
            this.ExportPatches_btn.UseVisualStyleBackColor = true;
            this.ExportPatches_btn.Click += new System.EventHandler(this.ExportCFile_Click);
            // 
            // tFirmwareName
            // 
            this.tFirmwareName.AutoSize = true;
            this.tFirmwareName.Location = new System.Drawing.Point(24, 58);
            this.tFirmwareName.Name = "tFirmwareName";
            this.tFirmwareName.Size = new System.Drawing.Size(0, 13);
            this.tFirmwareName.TabIndex = 4;
            // 
            // tFirmwareVersion
            // 
            this.tFirmwareVersion.AutoSize = true;
            this.tFirmwareVersion.Location = new System.Drawing.Point(24, 86);
            this.tFirmwareVersion.Name = "tFirmwareVersion";
            this.tFirmwareVersion.Size = new System.Drawing.Size(0, 13);
            this.tFirmwareVersion.TabIndex = 5;
            // 
            // tFeature
            // 
            this.tFeature.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.tFeature.Location = new System.Drawing.Point(12, 226);
            this.tFeature.MinimumSize = new System.Drawing.Size(100, 20);
            this.tFeature.Name = "tFeature";
            this.tFeature.Size = new System.Drawing.Size(373, 62);
            this.tFeature.TabIndex = 6;
            // 
            // lstFeatures
            // 
            this.lstFeatures.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.lstFeatures.DrawMode = System.Windows.Forms.DrawMode.OwnerDrawFixed;
            this.lstFeatures.FormattingEnabled = true;
            this.lstFeatures.Location = new System.Drawing.Point(12, 113);
            this.lstFeatures.Name = "lstFeatures";
            this.lstFeatures.Size = new System.Drawing.Size(373, 95);
            this.lstFeatures.TabIndex = 7;
            this.lstFeatures.DrawItem += new System.Windows.Forms.DrawItemEventHandler(this.ListBox1_DrawItem);
            this.lstFeatures.SelectedIndexChanged += new System.EventHandler(this.lstFeatures_SelectedIndexChanged);
            this.lstFeatures.DoubleClick += new System.EventHandler(this.lstFeatures_DoubleClick);
            // 
            // bSaveFirmware
            // 
            this.bSaveFirmware.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.bSaveFirmware.Enabled = false;
            this.bSaveFirmware.Location = new System.Drawing.Point(12, 291);
            this.bSaveFirmware.Name = "bSaveFirmware";
            this.bSaveFirmware.Size = new System.Drawing.Size(133, 23);
            this.bSaveFirmware.TabIndex = 8;
            this.bSaveFirmware.Text = "Save Firmware";
            this.bSaveFirmware.UseVisualStyleBackColor = true;
            this.bSaveFirmware.Click += new System.EventHandler(this.bSaveFirmware_Click);
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(397, 332);
            this.Controls.Add(this.bSaveFirmware);
            this.Controls.Add(this.lstFeatures);
            this.Controls.Add(this.tFeature);
            this.Controls.Add(this.tFirmwareVersion);
            this.Controls.Add(this.tFirmwareName);
            this.Controls.Add(this.ExportPatches_btn);
            this.Controls.Add(this.PatchFile_btn);
            this.Controls.Add(this.EncodeFile_btn);
            this.Controls.Add(this.DecodeFile_btn);
            this.Name = "MainForm";
            this.Text = "Form1";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button DecodeFile_btn;
        private System.Windows.Forms.Button EncodeFile_btn;
        private System.Windows.Forms.Button PatchFile_btn;
        private System.Windows.Forms.Button ExportPatches_btn;
        private System.Windows.Forms.Label tFirmwareName;
        private System.Windows.Forms.Label tFirmwareVersion;
        private System.Windows.Forms.Label tFeature;
        private System.Windows.Forms.ListBox lstFeatures;
        private System.Windows.Forms.Button bSaveFirmware;
    }
}

